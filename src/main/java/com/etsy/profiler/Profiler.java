package com.etsy.profiler;

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
     * Record a gauge value in StatsD
     *
     * @param key The key for the gauge
     * @param value The value of the gauge
     */
    protected void recordGaugeValue(String key, long value) {
        client.recordGaugeValue(key, value);
    }

    /**
     * Increment a count value in StatsD
     *
     * @param key The key for the count
     * @param increment The value by which to increment the count
     */
    protected void count(String key, long increment) {
        client.count(key, increment);
    }
}
