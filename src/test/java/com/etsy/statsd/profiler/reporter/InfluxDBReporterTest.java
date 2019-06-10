package com.etsy.statsd.profiler.reporter;

import com.etsy.statsd.profiler.Arguments;
import com.etsy.statsd.profiler.reporter.mock.BaseReporterTest;
import com.etsy.statsd.profiler.util.MockArguments;
import com.etsy.statsd.profiler.util.TagUtil;
import com.google.common.collect.ImmutableMap;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        assertEquals(1, args.length);

        BatchPoints actual = (BatchPoints) args[0];

        Point expectedPoint = Point.measurement("fake")
                .field(InfluxDBReporter.VALUE_COLUMN, 100L)
                .tag(TagUtil.PREFIX_TAG, "influxdb.reporter.test")
                .build();

        BatchPoints expected = BatchPoints.database("database").build();
        expected.point(expectedPoint);

        assertEquals(expected.getDatabase(), actual.getDatabase());
        assertEquals(expected.getPoints().size(), actual.getPoints().size());

        Point actualPoint = actual.getPoints().get(0);

        // All the fields on Point are private
        assertTrue(actualPoint.lineProtocol().startsWith("fake"));
        assertTrue(actualPoint.lineProtocol().contains("value=100"));
        assertTrue(actualPoint.lineProtocol().contains("prefix=influxdb.reporter.test"));
    }

    @Test
    public void testRecordGaugeValue() {
        Mockito.doAnswer(answer).when(client).write(Matchers.any(BatchPoints.class));
        reporter.recordGaugeValue("fake", 100L);
    }

    @Test
    public void testHttpsUrlResolution() {
        Arguments arguments = MockArguments.createArgs("localhost", 443, "influxdb.reporter.test",
                ImmutableMap.of("username", "user", "password", "password", "database", "database", "useHttps", "true"));
        InfluxDBReporter reporter  = new InfluxDBReporter(arguments);

        assertEquals(reporter.resolveUrl("localhost", 443), "https://localhost:443");
    }

    @Test
    public void testHttpUrlResolution() {
        Arguments arguments = MockArguments.createArgs("localhost", 8888, "influxdb.reporter.test",
                ImmutableMap.of("username", "user", "password", "password", "database", "database", "useHttps", "false"));
        InfluxDBReporter reporter  = new InfluxDBReporter(arguments);

        assertEquals(reporter.resolveUrl("localhost", 8888), "http://localhost:8888");
    }
}
