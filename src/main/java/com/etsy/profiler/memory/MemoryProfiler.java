package com.etsy.profiler.memory;

import com.etsy.profiler.Profiler;
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
public class MemoryProfiler implements Profiler {
    private StatsDClient client;
    private MemoryMXBean memoryMXBean;
    private List<GarbageCollectorMXBean> gcMXBeans;

    public MemoryProfiler(StatsDClient client) {
        this.client = client;
        memoryMXBean = ManagementFactory.getMemoryMXBean();
        gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
    }

    @Override
    /**
     * Profile memory usage and GC statistics
     */
    public void profile() {
        int finalizationPendingCount = memoryMXBean.getObjectPendingFinalizationCount();
        MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeap = memoryMXBean.getNonHeapMemoryUsage();

        client.recordGaugeValue("pending-finalization-count", finalizationPendingCount);
        recordMemoryUsage("heap", heap);
        recordMemoryUsage("nonheap", nonHeap);

        for (GarbageCollectorMXBean gcMXBean : gcMXBeans) {
            client.recordGaugeValue("gc." + gcMXBean.getName() + ".count", gcMXBean.getCollectionCount());
            client.recordGaugeValue("gc." + gcMXBean.getName() + ".time", gcMXBean.getCollectionTime());
        }
    }

    /**
     * Records memory usage
     *
     * @param prefix The prefix to use for this object
     * @param memory The MemoryUsage object containing the memory usage info
     */
    private void recordMemoryUsage(String prefix, MemoryUsage memory) {
        client.recordGaugeValue(prefix + ".init", memory.getInit());
        client.recordGaugeValue(prefix + ".used", memory.getUsed());
        client.recordGaugeValue(prefix + ".committed", memory.getCommitted());
        client.recordGaugeValue(prefix + ".max", memory.getMax());
    }
}
