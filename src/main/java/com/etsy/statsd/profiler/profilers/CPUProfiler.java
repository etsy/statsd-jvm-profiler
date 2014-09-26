package com.etsy.statsd.profiler.profilers;

import com.etsy.statsd.profiler.Profiler;
import com.etsy.statsd.profiler.util.StackTraceFilter;
import com.etsy.statsd.profiler.util.StackTraceFormatter;
import com.etsy.statsd.profiler.util.ThreadDumper;
import com.etsy.statsd.profiler.worker.ProfilerThreadFactory;
import com.google.common.base.Predicate;
import com.timgroup.statsd.StatsDClient;

import java.lang.management.ThreadInfo;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Profiles CPU time spent in each method
 *
 * @author Andrew Johnson
 */
public class CPUProfiler extends Profiler {
    public static final long PERIOD = 1;
    public static final List<String> EXCLUDE_PACKAGES = Arrays.asList("com.etsy.statsd.profiler", "com.timgroup.statsd");

    private Map<String, Long> methodCounts;
    private int profileCount;
    private StackTraceFilter filter;


    public CPUProfiler(StatsDClient client, List<String> filterPackages) {
        super(client);
        methodCounts = new HashMap<>();
        profileCount = 0;
        filter = new StackTraceFilter(filterPackages, EXCLUDE_PACKAGES);
    }

    /**
     * Profile CPU time by method call
     */
    @Override
    public void profile() {
        profileCount++;

        for (ThreadInfo thread : getAllRunnableThreads()) {
            // certain threads do not have stack traces
            if (thread.getStackTrace().length > 0) {
                String traceKey = StackTraceFormatter.formatStackTrace(thread.getStackTrace());
                if (filter.includeStackTrace(traceKey)) {
                    Long count = methodCounts.get(traceKey);
                    if (count == null) {
                        methodCounts.put(traceKey, PERIOD);
                    } else {
                        methodCounts.put(traceKey, count + PERIOD);
                    }
                }
            }
        }

        // To keep from overwhelming StatsD, we only report statistics every second
        if (profileCount % 1000 == 0) {
            recordMethodCounts();
        }
    }

    /**
     * Flush methodCounts data on shutdown
     */
    @Override
    public void flushData() {
        recordMethodCounts();
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
            recordGaugeValue("cpu.trace." + entry.getKey(), entry.getValue());
        }
    }

    /**
     * Gets all runnable threads, excluding profiler threads
     *
     * @return A Collection<ThreadInfo> representing current thread state
     */
    private Collection<ThreadInfo> getAllRunnableThreads() {
        return ThreadDumper.filterAllThreadsInState(false, false, Thread.State.RUNNABLE, new Predicate<ThreadInfo>() {
            @Override
            public boolean apply(ThreadInfo input) {
                return !input.getThreadName().startsWith(ProfilerThreadFactory.NAME_PREFIX);
            }
        });
    }
}
