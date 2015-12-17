package com.etsy.statsd.profiler.worker;

import com.etsy.statsd.profiler.Profiler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;

/**
 * Worker thread for executing a profiler
 *
 * @author Andrew Johnson
 */
public class ProfilerWorkerThread implements Runnable {
    private Profiler profiler;
    private LinkedList<String> errors;

    public ProfilerWorkerThread(Profiler profiler, LinkedList<String> errors) {
        this.profiler = profiler;
        this.errors = errors;
    }

    @Override
    public void run() {
        try {
            profiler.profile();
        } catch(Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            errors.add(String.format("Received an error running profiler: %s, error: %s", profiler.getClass().getName(), sw.toString()));
            if ( errors.size() > 10) {
                errors.pollFirst();
            }
        }
    }
}
