package com.etsy.statsd.profiler.reporter;

import com.etsy.statsd.profiler.Arguments;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;

/**
 * Reporter that sends data to StatsD
 *
 * @author Andrew Johnson
 */
public class StatsDReporter extends Reporter<StatsDClient> {
    public StatsDReporter(Arguments arguments) {
        super(arguments);
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

    /**
     * Construct a StatsD client
     *
     * @param server The hostname of the StatsD server
     * @param port The port on which StatsD is running
     * @param prefix The prefix for all metrics sent
     * @return A StatsD client
     */
    @Override
    protected StatsDClient createClient(String server, int port, String prefix) {
        return new NonBlockingStatsDClient(prefix, server, port);
    }

    /**
     * Handle additional arguments
     *
     * @param arguments The arguments given to the profiler agent
     */
    @Override
    protected void handleArguments(Arguments arguments) { }
}
