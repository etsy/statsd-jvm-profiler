package com.etsy.statsd.profiler.profilers;

import com.etsy.statsd.profiler.Arguments;
import com.etsy.statsd.profiler.Profiler;
import com.etsy.statsd.profiler.reporter.Reporter;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Profiles memory usage and GC statistics
 *
 * @author Andrew Johnson
 */
public class MemoryProfiler extends Profiler {
    public static final long PERIOD = 10;

    private MemoryMXBean memoryMXBean;
    private List<GarbageCollectorMXBean> gcMXBeans;

    public MemoryProfiler(Reporter reporter, Arguments arguments) {
        super(reporter, arguments);
        memoryMXBean = ManagementFactory.getMemoryMXBean();
        gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
    }

    /**
     * Profile memory usage and GC statistics
     */
    @Override
    public void profile() {
        recordStats();
    }

    @Override
    public void flushData() {
        recordStats();
    }

    @Override
    public long getPeriod() {
        return PERIOD;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return TimeUnit.SECONDS;
    }

    @Override
    protected void handleArguments(Arguments arguments) { /* No arguments needed */ }

    /**
     * Records all memory statistics
     */
    private void recordStats() {
        int finalizationPendingCount = memoryMXBean.getObjectPendingFinalizationCount();
        MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeap = memoryMXBean.getNonHeapMemoryUsage();

        recordGaugeValue("pending-finalization-count", finalizationPendingCount);
        recordMemoryUsage("heap", heap);
        recordMemoryUsage("nonheap", nonHeap);

        for (GarbageCollectorMXBean gcMXBean : gcMXBeans) {
            recordGaugeValue("gc." + gcMXBean.getName() + ".count", gcMXBean.getCollectionCount());
            recordGaugeValue("gc." + gcMXBean.getName() + ".time", gcMXBean.getCollectionTime());
        }
    }

    /**
     * Records memory usage
     *
     * @param prefix The prefix to use for this object
     * @param memory The MemoryUsage object containing the memory usage info
     */
    private void recordMemoryUsage(String prefix, MemoryUsage memory) {
        recordGaugeValue(prefix + ".init", memory.getInit());
        recordGaugeValue(prefix + ".used", memory.getUsed());
        recordGaugeValue(prefix + ".committed", memory.getCommitted());
        recordGaugeValue(prefix + ".max", memory.getMax());
    }
}
