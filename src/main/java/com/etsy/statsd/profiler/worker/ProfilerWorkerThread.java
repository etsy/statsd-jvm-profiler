package com.etsy.statsd.profiler.worker;

import com.etsy.statsd.profiler.Profiler;

/**
 * Worker thread for executing a profiler
 *
 * @author Andrew Johnson
 */
public class ProfilerWorkerThread implements Runnable {
    private Profiler profiler;

    public ProfilerWorkerThread(Profiler profiler) {
        this.profiler = profiler;
    }

    @Override
    public void run() {
        profiler.profile();
    }
}
