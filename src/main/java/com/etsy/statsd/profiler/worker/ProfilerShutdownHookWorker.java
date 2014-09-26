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

    public ProfilerShutdownHookWorker(Collection<Profiler> profilers, ScheduledExecutorService scheduledExecutorService) {
        scheduledExecutorService.shutdownNow();
        this.profilers = profilers;
    }

    @Override
    public void run() {
        for (Profiler p : profilers) {
            p.flushData();
        }
    }
}
