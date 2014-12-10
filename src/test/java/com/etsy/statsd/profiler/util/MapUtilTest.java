package com.etsy.statsd.profiler.util;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class MapUtilTest {

    @Test
    public void testSetOrIncrementMap() {
        Map<String, Long> map = new HashMap<>();
        MapUtil.setOrIncrementMap(map, "key", 1);
        assertEquals(new Long(1L), map.get("key"));

        MapUtil.setOrIncrementMap(map, "key", 1);
        assertEquals(new Long(2L), map.get("key"));

        MapUtil.setOrIncrementMap(map, "key2", 1);
        assertEquals(new Long(1L), map.get("key2"));
    }
}