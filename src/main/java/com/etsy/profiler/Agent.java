package com.etsy.profiler;

import com.etsy.profiler.profilers.CPUProfiler;
import com.etsy.profiler.profilers.MemoryProfiler;
import com.etsy.profiler.worker.ProfilerWorkerThread;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;

import java.lang.instrument.Instrumentation;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * javaagent profiler using StatsD as a backend
 *
 * @author Andrew Johnson
 */
public class Agent {
    public static final long PERIOD = 100;

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
        String prefix = argMap.get("prefix");

        StatsDClient client = new NonBlockingStatsDClient(prefix, statsdServer, statsdPort);
        Collection<Profiler> profilers = new ArrayList<>();
        profilers.add(new MemoryProfiler(client));
        profilers.add(new CPUProfiler(client));

        scheduleProfilers(profilers);
    }

    private static void scheduleProfilers(Collection<Profiler> profilers) {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(profilers.size());

        for (Profiler profiler : profilers) {
            ProfilerWorkerThread thread = new ProfilerWorkerThread(profiler);
            scheduledExecutorService.scheduleAtFixedRate(thread, 10, PERIOD, TimeUnit.MILLISECONDS);
        }
    }
}
