package com.etsy.statsd.profiler.reporter;

import com.etsy.statsd.profiler.Arguments;
import com.google.common.base.Preconditions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Serie;

import java.util.concurrent.TimeUnit;

/**
 * Reporter that sends data to InfluxDB
 *
 * @author Andrew Johnson
 */
public class InfluxDBReporter extends Reporter<InfluxDB> {
    public static final String VALUE_COLUMN = "value";
    public static final String USERNAME_ARG = "username";
    public static final String PASSWORD_ARG = "password";
    public static final String DATABASE_ARG = "database";

    private String prefix;
    private String username;
    private String password;
    private String database;

    public InfluxDBReporter(Arguments arguments) {
        super(arguments);
        this.prefix = arguments.metricsPrefix;
    }

    /**
     * Record a gauge value in InfluxDB
     *
     * @param key The key for the gauge
     * @param value The value of the gauge
     */
    @Override
    public void recordGaugeValue(String key, long value) {
        Serie s = new Serie.Builder(String.format("%s.%s", prefix, key))
                .columns(VALUE_COLUMN)
                .values(value)
                .build();
        client.write(database, TimeUnit.MILLISECONDS, s);
    }

    /**
     *
     * @param server The server to which to report data
     * @param port The port on which the server is running
     * @param prefix The prefix for metrics
     * @return An InfluxDB client
     */
    @Override
    protected InfluxDB createClient(String server, int port, String prefix) {
        return InfluxDBFactory.connect(String.format("http://%s:%d", server, port), username, password);
    }

    /**
     * Handle remaining arguments
     *
     * @param arguments The arguments given to the profiler agent
     */
    @Override
    protected void handleArguments(Arguments arguments) {
        username = arguments.remainingArgs.get(USERNAME_ARG);
        password = arguments.remainingArgs.get(PASSWORD_ARG);
        database = arguments.remainingArgs.get(DATABASE_ARG);

        Preconditions.checkNotNull(username);
        Preconditions.checkNotNull(password);
        Preconditions.checkNotNull(database);
    }
}
