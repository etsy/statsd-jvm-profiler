package com.etsy.statsd.profiler.worker;

import com.etsy.statsd.profiler.Profiler;

import java.util.Collection;

/**
 * Worker thread for profiler shutdown hook
 *
 * @author Andrew Johnson
 */
public class ProfilerShutdownHookWorker implements Runnable {
    private Collection<Profiler> profilers;

    public ProfilerShutdownHookWorker(Collection<Profiler> profilers) {
        this.profilers = profilers;
    }

    @Override
    public void run() {
        for (Profiler p : profilers) {
            p.flushData();
        }
    }
}
