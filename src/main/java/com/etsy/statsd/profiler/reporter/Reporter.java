package com.etsy.statsd.profiler.reporter;

import com.etsy.statsd.profiler.Arguments;
import com.google.common.base.Preconditions;

/**
 * Interface for reporters
 *
 * @author Andrew Johnson
 */
public abstract class Reporter<T> {
    public static final Class<?>[] CONSTRUCTOR_PARAM_TYPES =new Class<?>[]{Arguments.class};

    /**
     * The underlying implementation for this reporter
     */
    protected T client;

    public Reporter(Arguments arguments) {
        Preconditions.checkNotNull(arguments);
        handleArguments(arguments);
        client = createClient(arguments.server, arguments.port, arguments.metricsPrefix);
    }

    /**
     * Record a gauge value
     *
     * @param key The name of the gauge
     * @param value The value of the gauge
     */
    public abstract void recordGaugeValue(String key, long value);

    /**
     * Construct the underlying client implementation for this reporter
     *
     * @param server The server to which to report data
     * @param port The port on which the server is running
     * @param prefix The prefix for metrics
     * @return An instance of T, the client implementation
     */
    protected abstract T createClient(String server, int port, String prefix);

    /**
     * Handle any additional arguments necessary for this reporter
     *
     * @param arguments The arguments given to the profiler agent
     */
    protected abstract void handleArguments(Arguments arguments);
}
