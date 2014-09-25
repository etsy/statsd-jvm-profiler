package com.etsy.statsd.profiler.profilers;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class MemoryProfilerTest extends ProfilerTestBase {
    @Test
    public void testProfile() {
        MemoryProfiler profiler = new MemoryProfiler(client);
        profiler.profile();
        profiler.flushData();

        assertMessages(getExpectedMessages());
    }

    private Map<String, Long> getExpectedMessages() {
        Map<String, Long> expected = new HashMap<>();
        expected.put("mock.nonheap.init:dummy|g", 1L);
        expected.put("mock.gc.PS Scavenge.time:dummy|g", 1L);
        expected.put("mock.nonheap.committed:dummy|g", 1L);
        expected.put("mock.gc.PS MarkSweep.count:dummy|g", 1L);
        expected.put("mock.nonheap.max:dummy|g", 1L);
        expected.put("mock.heap.init:dummy|g", 1L);
        expected.put("mock.heap.used:dummy|g", 1L);
        expected.put("mock.heap.committed:dummy|g", 1L);
        expected.put("mock.gc.PS Scavenge.count:dummy|g", 1L);
        expected.put("mock.pending-finalization-count:dummy|g", 1L);
        expected.put("mock.nonheap.used:dummy|g", 1L);
        expected.put("mock.gc.PS MarkSweep.time:dummy|g", 1L);
        expected.put("mock.heap.max:dummy|g", 1L);

        return expected;
    }
}