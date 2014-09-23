package com.etsy.statsd.profiler;

import com.timgroup.statsd.StatsDClient;

/**
 * Interface for profilers
 *
 * @author Andrew Johnson
 */
public abstract class Profiler {
    private StatsDClient client;

    public Profiler(StatsDClient client) {
        this.client = client;
    }

    /**
     * Perform profiling
     */
    public abstract void profile();

    /**
     * Hook to flush any remaining data cached by the profiler at JVM shutdown
     */
    public abstract void flushData();

    /**
     * Get the period to use for this profiler in the ScheduledExecutorService
     *
     * @return The ScheduledExecutorThread period for this profiler
     */
    public abstract long getPeriod();

    /**
     * Record a gauge value in StatsD
     *
     * @param key The key for the gauge
     * @param value The value of the gauge
     */
    protected void recordGaugeValue(String key, long value) {
        client.recordGaugeValue(key, value);
    }
}
