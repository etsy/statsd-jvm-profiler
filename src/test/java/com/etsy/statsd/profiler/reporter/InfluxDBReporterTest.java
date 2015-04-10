package com.etsy.statsd.profiler.reporter;

import com.etsy.statsd.profiler.Arguments;
import com.etsy.statsd.profiler.reporter.mock.BaseReporterTest;
import com.etsy.statsd.profiler.util.MockArguments;
import com.google.common.collect.ImmutableMap;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Serie;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class InfluxDBReporterTest extends BaseReporterTest<InfluxDBReporter> {
    @Mock
    private InfluxDB client;

    @Override
    protected InfluxDBReporter constructReporter() {
        Arguments arguments = MockArguments.createArgs("localhost", 8888, "influxdb.reporter.test",
                ImmutableMap.of("username", "user", "password", "password", "database", "database"));
        return new InfluxDBReporter(arguments);
    }

    @Override
    protected void testCase(Object[] args) {
        assertEquals(3, args.length);
        assertEquals("database", args[0]);
        assertEquals(TimeUnit.MILLISECONDS, args[1]);

        Serie expected = new Serie.Builder("influxdb.reporter.test.fake")
                .columns(InfluxDBReporter.VALUE_COLUMN)
                .values(100L)
                .build();
        assertEquals(expected.getName(), ((Serie)args[2]).getName());
        assertArrayEquals(expected.getColumns(), ((Serie) args[2]).getColumns());
        assertEquals(expected.getRows(), ((Serie)args[2]).getRows());
    }

    @Test
    public void testRecordGaugeValue() {
        Mockito.doAnswer(answer).when(client).write(Matchers.anyString(), Matchers.any(TimeUnit.class), Matchers.any(Serie[].class));
        reporter.recordGaugeValue("fake", 100L);
    }
}