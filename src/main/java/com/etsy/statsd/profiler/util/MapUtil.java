package com.etsy.statsd.profiler.util;

import java.util.Map;

/**
 * Utility class for working with Maps
 *
 * @author Andrew Johnson
 */
public class MapUtil {

    /**
     * Set a new value in a map or increment an existing value
     *
     * @param map The map in which to modify the value
     * @param key The key for the map
     * @param inc The new value or increment for the given key
     */
    public static void setOrIncrementMap(Map<String, Long> map, String key, long inc) {
        Long val = map.get(key);
        if (val == null) {
            map.put(key, inc);
        } else {
            map.put(key, val + inc);
        }
    }
}
