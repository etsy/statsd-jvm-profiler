package com.etsy.statsd.profiler.worker;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * ThreadFactory for the profiler threads
 * This factory prefixes the thread name with 'statsd-jvm-profiler'
 * This allows the profilers to identify other profiler threads
 *
 * @author Andrew Johnson
 */
public class ProfilerThreadFactory implements ThreadFactory {
    public static final String NAME_PREFIX = "statsd-jvm-profiler";

    private ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();

    /**
     * Create a profiler thread with the name prefixed with 'statsd-jvm-profiler'
     * @param r A runnable to be executed by new thread instance
     * @return Constructed thread, or {@code null} if the request to
     *         create a thread is rejected
     */
    @Override
    public Thread newThread(Runnable r) {
        Thread t = defaultThreadFactory.newThread(r);
        if (t != null) {
            t.setName(String.format("%s-%s", NAME_PREFIX, t.getName()));
        }

        return t;
    }
}
