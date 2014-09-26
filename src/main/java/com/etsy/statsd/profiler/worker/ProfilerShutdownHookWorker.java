package com.etsy.statsd.profiler.worker;

import com.etsy.statsd.profiler.Profiler;

import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Worker thread for profiler shutdown hook
 *
 * @author Andrew Johnson
 */
public class ProfilerShutdownHookWorker implements Runnable {
    private Collection<Profiler> profilers;
    private ScheduledExecutorService scheduledExecutorService;

    public ProfilerShutdownHookWorker(Collection<Profiler> profilers, ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = scheduledExecutorService;
        this.profilers = profilers;
    }

    @Override
    public void run() {
        scheduledExecutorService.shutdownNow();
        for (Profiler p : profilers) {
            p.flushData();
        }
    }
}
