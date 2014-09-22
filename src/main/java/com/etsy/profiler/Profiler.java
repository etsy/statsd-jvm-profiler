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
     * Record execution time in StatsD
     *
     * @param key The key for the timer
     * @param increment The execution time to record
     */
    protected void recordExecutionTime(String key, long increment) {
        client.recordExecutionTime(key, increment * 1000);
    }
}
