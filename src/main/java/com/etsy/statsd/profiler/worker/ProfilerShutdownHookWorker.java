package com.etsy.statsd.profiler.worker;

import com.etsy.statsd.profiler.Profiler;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Worker thread for profiler shutdown hook
 *
 * @author Andrew Johnson
 */
public class ProfilerShutdownHookWorker implements Runnable {
    private Collection<Profiler> profilers;
    private AtomicReference<Boolean> isRunning;
    public ProfilerShutdownHookWorker(Collection<Profiler> profilers, AtomicReference<Boolean> isRunning) {
        this.profilers = profilers;
        this.isRunning = isRunning;
    }

    @Override
    public void run() {
        for (Profiler p : profilers) {
            p.flushData();
        }

        isRunning.set(false);
    }
}
