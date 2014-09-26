package com.etsy.statsd.profiler.profilers;

import com.etsy.statsd.profiler.Profiler;
import com.timgroup.statsd.StatsDClient;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Profiles CPU time spent in each method
 *
 * @author Andrew Johnson
 */
public class CPUProfiler extends Profiler {
    public static final long PERIOD = 1;

    private ThreadMXBean threadMXBean;
    private Map<String, Long> methodCounts;
    private int profileCount;
    private Set<String> seenTraces;
    private long filtered = 0L;
    private long emitted = 0L;

    public CPUProfiler(StatsDClient client) {
        super(client);
        threadMXBean = ManagementFactory.getThreadMXBean();
        methodCounts = new HashMap<>();
        profileCount = 0;
        seenTraces = new HashSet<>();
    }

    /**
     * Profile CPU time by method call
     */
    @Override
    public void profile() {
        profileCount++;
        List<ThreadInfo> threads = getAllRunnableThreads();

        for (ThreadInfo thread : threads) {
            // certain threads do not have stack traces
            if (thread.getStackTrace().length > 0) {
                String traceKey = formatStackTrace(thread.getStackTrace());
                // exclude other profilers from reporting
                if (!traceKey.contains("com-etsy-statsd-profiler")) {
                    if (!seenTraces.contains(traceKey)) {
                        if (traceKey.contains("com-etsy")) {
                            emitted++;
                        } else {
                            filtered++;
                        }

                        seenTraces.add(traceKey);
                    }
//                    Long count = methodCounts.get(traceKey);
//                    if (count == null) {
//                        methodCounts.put(traceKey, PERIOD);
//                    } else {
//                        methodCounts.put(traceKey, count + PERIOD);
//                    }
                }
            }
        }

        // To keep from overwhelming StatsD, we only report statistics every second
//        if (profileCount % 1000 == 0) {
//            recordMethodCounts();
//        }
    }

    /**
     * Flush methodCounts data on shutdown
     */
    @Override
    public void flushData() {
        recordGaugeValue("filtered", filtered);
        recordGaugeValue("emitted", emitted);

//        recordMethodCounts();
    }

    @Override
    public long getPeriod() {
        return PERIOD;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return TimeUnit.MILLISECONDS;
    }

    /**
     * Records method CPU time in StatsD
     */
    private void recordMethodCounts() {
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
        return String.format("%s-%s", element.getClassName().replace(".", "-"), element.getMethodName());
    }

    /**
     * Formats an entire stack trace as a String
     *
     * @param stack The stack trace to format
     * @return A String representing the given stack trace
     */
    private String formatStackTrace(StackTraceElement[] stack) {
        ArrayUtils.reverse(stack); // reverse in place
        List<String> lines = new ArrayList<>();
        for (StackTraceElement element : stack) {
            lines.add(formatStackTraceElement(element));
        }

        return StringUtils.join(lines, ".");
    }

    /**
     * Gets all runnable threads, excluding the current thread
     *
     * @return A List<ThreadInfo>
     */
    private List<ThreadInfo> getAllRunnableThreads() {
        List<ThreadInfo> threads = new ArrayList<>();
        for (ThreadInfo t : threadMXBean.dumpAllThreads(false, false)) {
            // We will sample all runnable threads that are not the current thread
            if (t.getThreadState() == Thread.State.RUNNABLE && t.getThreadId() != Thread.currentThread().getId()) {
                threads.add(t);
            }
        }

        return threads;
    }
}
