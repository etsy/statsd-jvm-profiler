package com.etsy.statsd.profiler.util;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CPUTracesTest {
    private CPUTraces traces;

    @Before
    public void setup() {
        traces = new CPUTraces();
    }

    @Test
    public void testGetDataToFlush() {
        traces.increment("cpu.trace.key", 1);
        traces.increment("cpu.trace.key2", 3);

        Map<String, Long> expectedMap = new HashMap<>();
        expectedMap.put("cpu.trace.key", 1L);
        expectedMap.put("cpu.trace.key2", 3L);

        assertEquals(expectedMap, traces.getDataToFlush());

        traces.increment("cpu.trace.key3", 100);
        traces.increment("cpu.trace.key2", 3);

        expectedMap = new HashMap<>();
        expectedMap.put("cpu.trace.key2", 3L);
        expectedMap.put("cpu.trace.key3", 100L);

        assertEquals(expectedMap, traces.getDataToFlush());
    }

    @Test
    public void testGetBounds() {
        traces.increment("cpu.trace.a.b.c", 1);
        traces.increment("cpu.trace.a.b.c.d", 1);
        traces.increment("cpu.trace.a.b.c.d.e", 1);

        Range bounds = traces.getBounds();
        assertEquals(3, bounds.getLeft());
        assertEquals(5, bounds.getRight());
    }
}
