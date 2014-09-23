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
     * Record a gauge value in StatsD
     *
     * @param key The key for the gauge
     * @param value The value of the gauge
     */
    protected void recordGaugeValue(String key, long value) {
        client.recordGaugeValue(key, value);
    }

    /**
     * Record a gauge delta in StatsD
     *
     * @param key The key for the gauge
     * @param delta The delta of the gauge
     */
    protected void recordGaugeDelta(String key, long delta) {
        client.recordGaugeDelta(key, delta);
    }

    /**
     * Record execution time in StatsD
     *
     * @param key The key for the timer
     * @param ms The execution time to record
     */
    protected void recordExecutionTime(String key, long ms) {
        client.recordExecutionTime(key, ms);
    }

    protected void count(String key, long increment) {
        client.count(key, increment);
    }
}
