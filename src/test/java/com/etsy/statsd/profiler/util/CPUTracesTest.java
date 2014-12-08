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
    public void testSetOrIncrementMap() {
        Map<String, Long> map = new HashMap<>();
        traces.setOrIncrementMap(map, "key", 1);
        assertEquals(new Long(1L), map.get("key"));

        traces.setOrIncrementMap(map, "key", 1);
        assertEquals(new Long(2L), map.get("key"));

        traces.setOrIncrementMap(map, "key2", 1);
        assertEquals(new Long(1L), map.get("key2"));
    }

    @Test
    public void testGetDataToFlush() {
        traces.increment("key", 1);
        traces.increment("key2", 3);

        Map<String, Long> expectedMap = new HashMap<>();
        expectedMap.put("key", 1L);
        expectedMap.put("key2", 3L);

        assertEquals(expectedMap.entrySet(), traces.getDataToFlush(false));

        traces.increment("key", 1);
        traces.increment("key2", 3);

        expectedMap = new HashMap<>();
        expectedMap.put("key", 1L);
        expectedMap.put("key2", 3L);

        assertEquals(expectedMap.entrySet(), traces.getDataToFlush(false));

        traces.increment("key", 1);
        traces.increment("key2", 3);

        expectedMap = new HashMap<>();
        expectedMap.put("key", 3L);
        expectedMap.put("key2", 9L);

        assertEquals(expectedMap.entrySet(), traces.getDataToFlush(true));
    }
}