package com.etsy.statsd.profiler.reporter;

import com.etsy.statsd.profiler.Arguments;
import com.etsy.statsd.profiler.profilers.MockReportingProfiler;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ReporterTest {
    @Test
    public void testReporter() {
        MockReporter mockReporter = new MockReporter();
        MockReportingProfiler profiler = new MockReportingProfiler(mockReporter);

        profiler.profile();
        profiler.flushData();

        Map<String, Long> expected = new HashMap<>();
        expected.put("profile", 1L);
        expected.put("flushData", 1L);
        assertEquals(expected, mockReporter.getOutput());
    }

    @Test(expected = NullPointerException.class)
    public void testNullServerArg() {
        new Reporter<String>(null, 1, "", null) {
            @Override
            public void recordGaugeValue(String key, long value) { }

            @Override
            protected String createClient(String server, int port, String prefix) {
                return null;
            }

            @Override
            protected void handleArguments(Arguments arguments) { }
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPortArg() {
        new Reporter<String>("", 0, "", null) {
            @Override
            public void recordGaugeValue(String key, long value) { }

            @Override
            protected String createClient(String server, int port, String prefix) {
                return null;
            }

            @Override
            protected void handleArguments(Arguments arguments) { }
        };
    }

    @Test(expected = NullPointerException.class)
    public void testNullPrefixArg() {
        new Reporter<String>("", 1, null, null) {
            @Override
            public void recordGaugeValue(String key, long value) { }

            @Override
            protected String createClient(String server, int port, String prefix) {
                return null;
            }

            @Override
            protected void handleArguments(Arguments arguments) { }
        };
    }
}