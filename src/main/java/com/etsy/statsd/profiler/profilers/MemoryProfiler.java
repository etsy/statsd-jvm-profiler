package com.etsy.statsd.profiler.profilers;

import com.etsy.statsd.profiler.Profiler;
import com.timgroup.statsd.StatsDClient;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

/**
 * Profiles memory usage and GC statistics
 *
 * @author Andrew Johnson
 */
public class MemoryProfiler extends Profiler {
    private MemoryMXBean memoryMXBean;
    private List<GarbageCollectorMXBean> gcMXBeans;

    public MemoryProfiler(StatsDClient client) {
        super(client);
        memoryMXBean = ManagementFactory.getMemoryMXBean();
        gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
    }

    /**
     * Profile memory usage and GC statistics
     */
    @Override
    public void profile() {
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
