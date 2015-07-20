package com.etsy.statsd.profiler.profilers;

import com.etsy.statsd.profiler.Arguments;
import com.etsy.statsd.profiler.Profiler;
import com.etsy.statsd.profiler.reporter.Reporter;
import com.google.common.collect.Maps;

import java.lang.management.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Profiles memory usage and GC statistics
 *
 * @author Andrew Johnson
 */
public class MemoryProfiler extends Profiler {
    public static final long PERIOD = 10;

    private MemoryMXBean memoryMXBean;
    private List<GarbageCollectorMXBean> gcMXBeans;
    private HashMap<GarbageCollectorMXBean, AtomicLong> gcTimes = new HashMap<>();
    private ClassLoadingMXBean classLoadingMXBean;
    private List<MemoryPoolMXBean> memoryPoolMXBeans;

    public MemoryProfiler(Reporter reporter, Arguments arguments) {
        super(reporter, arguments);
        memoryMXBean = ManagementFactory.getMemoryMXBean();
        gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();

        for (GarbageCollectorMXBean b : gcMXBeans) gcTimes.put(b, new AtomicLong());
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
        long finalizationPendingCount = memoryMXBean.getObjectPendingFinalizationCount();
        MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeap = memoryMXBean.getNonHeapMemoryUsage();
        Map<String, Long> metrics = Maps.newHashMap();

        metrics.put("pending-finalization-count", finalizationPendingCount);
        recordMemoryUsage("heap.total", heap, metrics);
        recordMemoryUsage("nonheap.total", nonHeap, metrics);

        for (GarbageCollectorMXBean gcMXBean : gcMXBeans) {
            String gcName = gcMXBean.getName().replace(" ", "_");
            metrics.put("gc." + gcName + ".count", gcMXBean.getCollectionCount());

            final long time = gcMXBean.getCollectionTime();
            final long prevTime = gcTimes.get(gcMXBean).get();
            final long runtime = time - prevTime;

            metrics.put("gc." + gcName + ".time", time);
            metrics.put("gc." + gcName + ".runtime", runtime);

            if (runtime > 0) gcTimes.get(gcMXBean).set(time);
        }

        long loadedClassCount = classLoadingMXBean.getLoadedClassCount();
        long totalLoadedClassCount = classLoadingMXBean.getTotalLoadedClassCount();
        long unloadedClassCount = classLoadingMXBean.getUnloadedClassCount();

        metrics.put("loaded-class-count", loadedClassCount);
        metrics.put("total-loaded-class-count", totalLoadedClassCount);
        metrics.put("unloaded-class-count", unloadedClassCount);

        for (MemoryPoolMXBean memoryPoolMXBean: memoryPoolMXBeans) {
            String type = poolTypeToMetricName(memoryPoolMXBean.getType());
            String name = poolNameToMetricName(memoryPoolMXBean.getName());
            String prefix = type + '.' + name;
            MemoryUsage usage = memoryPoolMXBean.getUsage();

            recordMemoryUsage(prefix, usage, metrics);
        }
        
        recordGaugeValues(metrics);
    }

    /**
     * Records memory usage
     *
     * @param prefix The prefix to use for this object
     * @param memory The MemoryUsage object containing the memory usage info
     */
    private void recordMemoryUsage(String prefix, MemoryUsage memory, Map<String, Long> metrics) {
        metrics.put(prefix + ".init", memory.getInit());
        metrics.put(prefix + ".used", memory.getUsed());
        metrics.put(prefix + ".committed", memory.getCommitted());
        metrics.put(prefix + ".max", memory.getMax());
    }

    /**
     * Formats a MemoryType into a valid metric name
     *
     * @param memoryType a MemoryType
     * @return a valid metric name
     */
    private String poolTypeToMetricName(MemoryType memoryType) {
        switch (memoryType) {
            case HEAP:
                return "heap";
            case NON_HEAP:
                return "nonheap";
            default:
                return "unknown";
        }
    }

    /**
     * Formats a pool name into a valid metric name
     *
     * @param poolName a pool name
     * @return a valid metric name
     */
    private String poolNameToMetricName(String poolName) {
        return poolName.toLowerCase().replaceAll("\\s+", "-");
    }
}
