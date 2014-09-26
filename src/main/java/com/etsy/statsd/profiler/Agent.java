package com.etsy.statsd.profiler;

import com.etsy.statsd.profiler.profilers.CPUProfiler;
import com.etsy.statsd.profiler.profilers.MemoryProfiler;
import com.etsy.statsd.profiler.worker.ProfilerShutdownHookWorker;
import com.etsy.statsd.profiler.worker.ProfilerThreadFactory;
import com.etsy.statsd.profiler.worker.ProfilerWorkerThread;
import com.google.common.util.concurrent.MoreExecutors;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * javaagent profiler using StatsD as a backend
 *
 * @author Andrew Johnson
 */
public class Agent {
    public static final int EXECUTOR_DELAY = 0;

    /**
     * Start the profiler
     *
     * @param args Profiler arguments
     * @param instrumentation Instrumentation agent
     */
    public static void premain(final String args, final Instrumentation instrumentation) {
        Arguments arguments = Arguments.parseArgs(args);
        String statsdServer = arguments.statsdServer;
        int statsdPort = arguments.statsdPort;
        String prefix = arguments.metricsPrefix.or("statsd-jvm-profiler");
        List<String> filterPackages = arguments.filterPackages.or(new ArrayList<String>());

        StatsDClient client = new NonBlockingStatsDClient(prefix, statsdServer, statsdPort);

        Profiler memoryProfiler = new MemoryProfiler(client);
        Profiler cpuProfiler = new CPUProfiler(client, filterPackages);
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
        ScheduledExecutorService scheduledExecutorService = MoreExecutors.getExitingScheduledExecutorService(
                (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(profilers.size(), new ProfilerThreadFactory()));

        for (Profiler profiler : profilers) {
            ProfilerWorkerThread worker = new ProfilerWorkerThread(profiler);
            scheduledExecutorService.scheduleAtFixedRate(worker, EXECUTOR_DELAY, profiler.getPeriod(), profiler.getTimeUnit());
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
