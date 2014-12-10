package com.etsy.statsd.profiler.reporter;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;

/**
 * Reporter that sends data to StatsD
 *
 * @author Andrew Johnson
 */
public class StatsDReporter implements Reporter {
    private StatsDClient client;

    public StatsDReporter(String server, int port, String prefix) {
        client = new NonBlockingStatsDClient(prefix, server, port);
    }

    /**
     * Record a gauge value in StatsD
     *
     * @param key The key for the gauge
     * @param value The value of the gauge
     */
    @Override
    public void recordGaugeValue(String key, long value) {
        client.recordGaugeValue(key, value);
    }
}
