package com.etsy.statsd.profiler.profilers;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class CPUProfilerTest extends ProfilerTestBase {
    @Test
    public void testProfile() {
        CPUProfiler profiler = new CPUProfiler(client);
        profiler.profile();
        profiler.flushData();

        // We can't verify specific messages because the cpu profile
        // will be different depending on how the test is run
        assertMessageTypes(getExpectedCountsByType());
    }

    private Map<String, Long> getExpectedCountsByType() {
        Map<String, Long> expected = new HashMap<>();
        expected.put("g", 1L);

        return expected;
    }
}