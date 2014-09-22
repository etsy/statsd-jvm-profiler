package com.etsy.profiler.worker;

import com.etsy.profiler.Profiler;

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
