package com.etsy.statsd.profiler;

import com.etsy.statsd.profiler.profilers.CPUProfiler;
import com.etsy.statsd.profiler.profilers.MemoryProfiler;
import com.etsy.statsd.profiler.worker.ProfilerShutdownHookWorker;
import com.etsy.statsd.profiler.worker.ProfilerWorkerThread;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * javaagent profiler using StatsD as a backend
 *
 * @author Andrew Johnson
 */
public class Agent {
    public static final int NUM_EXECUTOR_THREADS = 2;
    public static final int EXECUTOR_DELAY = 0;


    /**
     * Parses profiler arguments into a map
     *
     * @param args A string containing the arguments
     * @return A map of argument names to values
     */
    private static Map<String, String> parseArgs(final String args) {
        Map<String, String> parsed = new HashMap<>();
        for (String argPair : args.split(",")) {
            String[] tokens = argPair.split("=");
            if (tokens.length != 2) {
                throw new IllegalArgumentException("statsd-jvm-profiler takes a comma-delimited list of arguments in k=v form");
            }

            parsed.put(tokens[0], tokens[1]);
        }

        return parsed;
    }

    /**
     * Start the profiler
     *
     * @param args Profiler arguments
     * @param instrumentation Instrumentation agent
     */
    public static void premain(final String args, final Instrumentation instrumentation) {
        Map<String, String> argMap = parseArgs(args);
        String statsdServer = argMap.get("server");
        int statsdPort = Integer.valueOf(argMap.get("port"));
        String user = argMap.get("user");
        String flowId = argMap.get("flowid");
        String stepId = argMap.get("stepid");
        String prePrefix = argMap.get("prefix");
        String prefix = String.format("%s.%s.%s.%s", prePrefix, user, flowId, stepId);

        StatsDClient client = new NonBlockingStatsDClient(prefix, statsdServer, statsdPort);

        Profiler memoryProfiler = new MemoryProfiler(client);
        Profiler cpuProfiler = new CPUProfiler(client);
        Collection<Profiler> profilers = Arrays.asList(memoryProfiler, cpuProfiler);

        scheduleProfilers(profilers);
        registerShutdownHook(profilers);
    }

    /**
     * Schedule profilers with a SchedulerExecutorService
     *
     * @param profilers Collection of profilers to schedule
     */
    private static void scheduleProfilers(Collection<Profiler> profilers) {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(NUM_EXECUTOR_THREADS);

        for (Profiler profiler : profilers) {
            ProfilerWorkerThread worker = new ProfilerWorkerThread(profiler);
            scheduledExecutorService.scheduleAtFixedRate(worker, EXECUTOR_DELAY, profiler.getPeriod(), TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Register a shutdown hook to flush profiler data to StatsD
     *
     * @param profilers The profilers to flush at shutdown
     */
    private static void registerShutdownHook(Collection<Profiler> profilers) {
        Thread shutdownHook = new Thread(new ProfilerShutdownHookWorker(profilers));
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }
}
