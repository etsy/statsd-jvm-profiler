package com.etsy.statsd.profiler.util;

import java.util.Map;

/**
 * Utility class for working with Maps
 *
 * @author Andrew Johnson
 */
public final class MapUtil {
    private MapUtil() { }

    /**
     * Set a new value in a map or increment an existing value
     *
     * @param map The map in which to modify the value
     * @param key The key for the map
     * @param inc The new value or increment for the given key
     */
    public static void setOrIncrementMap(Map<String, Number> map, String key, Number inc) {
        Number val = map.get(key);
        if (val == null) {
            if (inc instanceof Double) {
                map.put(key, inc.doubleValue());
            } else if (inc instanceof Long || inc instanceof Integer) {
                map.put(key, inc.longValue());
            } else {
                throw new IllegalArgumentException("Unexpected Number type: " + inc.getClass().getSimpleName());
            }
        } else {
            if (val instanceof Double) {
                map.put(key, val.doubleValue() + inc.doubleValue());
            } else if (val instanceof Long || val instanceof Integer) {
                map.put(key, val.longValue() + inc.longValue());
            } else {
                throw new IllegalArgumentException("Unexpected Number type: " + val.getClass().getSimpleName());
            }
        }
    }
}
