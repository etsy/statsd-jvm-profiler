package com.etsy.statsd.profiler.reporter;

import com.etsy.statsd.profiler.profilers.MockReportingProfiler;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

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
}