package com.etsy.statsd.profiler.reporter;

/**
 * Interface for reporters
 *
 * @author Andrew Johnson
 */
public interface Reporter {
    /**
     * Record a gauge value
     *
     * @param key The name of the gauge
     * @param value The value of the gauge
     */
    void recordGaugeValue(String key, long value);
}
