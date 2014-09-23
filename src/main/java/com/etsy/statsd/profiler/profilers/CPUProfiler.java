package com.etsy.statsd.profiler.profilers;

import com.etsy.statsd.profiler.Agent;
import com.etsy.statsd.profiler.Profiler;
import com.timgroup.statsd.StatsDClient;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Profiles CPU time spent in each method
 *
 * @author Andrew Johnson
 */
public class CPUProfiler extends Profiler {
    private ThreadMXBean threadMXBean;
    Map<String, Long> methodCounts;

    public CPUProfiler(StatsDClient client) {
        super(client);
        threadMXBean = ManagementFactory.getThreadMXBean();
        methodCounts = new HashMap<>();
    }

    @Override
    public void profile() {
        List<ThreadInfo> threads = getAllRunnableThreads();

        for (ThreadInfo thread : threads) {
            for (StackTraceElement element : thread.getStackTrace()) {
                String methodKey = formatStackTraceElement(element);
                // exclude other profilers from reporting
                if (!(methodKey.startsWith("com.etsy.statsd.profiler") || methodKey.startsWith("com.timgroup.statsd"))) {
                    Long count = methodCounts.get(methodKey);
                    if (count == null) {
                        methodCounts.put(methodKey, Agent.PERIOD);
                    } else {
                        methodCounts.put(methodKey, count + Agent.PERIOD);
                    }
                }
            }
        }

        for (Map.Entry<String, Long> entry : methodCounts.entrySet()) {
            recordGaugeValue("cpu.method." + entry.getKey(), entry.getValue());
        }
    }

    /**
     * Formats a StackTraceElement as a String, excluding the line number
     *
     * @param element The StackTraceElement to format
     * @return A String representing the given StackTraceElement
     */
    private String formatStackTraceElement(StackTraceElement element) {
        return String.format("%s.%s", element.getClassName(), element.getMethodName());
    }

    /**
     * Gets all runnable threads, excluding the current thread
     *
     * @return A List<ThreadInfo>
     */
    private List<ThreadInfo> getAllRunnableThreads() {
        List<ThreadInfo> threads = new ArrayList<>();
        for (ThreadInfo t : threadMXBean.dumpAllThreads(false, false)) {
            // We will sample all runnable threads that are not this one
            if (t.getThreadState() == Thread.State.RUNNABLE && t.getThreadId() != Thread.currentThread().getId()) {
                threads.add(t);
            }
        }

        return threads;
    }
}
