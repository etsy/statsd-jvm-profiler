package com.etsy.statsd.profiler.util;


import com.google.common.collect.ImmutableMap;

import java.util.*;

/**
 * Represents the state of the CPU profiler
 *
 * @author Andrew Johnson
 */
public class CpuTraces {
    private Map<String, Long> traces;
    private Map<String, Long> deltas;
    private Set<String> dirtyTraces;

    public CpuTraces() {
        traces = new HashMap<>();
        deltas = new HashMap<>();
        dirtyTraces = new HashSet<>();
    }

    /**
     * Increment the aggregate time for a trace
     *
     * @param traceKey The key for the trace
     * @param inc The value by which to increment the aggregate time for the trace
     */
    public void increment(String traceKey, long inc) {
        setOrIncrementMap(deltas, traceKey, inc);
        dirtyTraces.add(traceKey);
    }

    /**
     * Get data to be flushed from the state
     * By default it only returns the deltas and updates the aggregate counts
     * But with the `flushAll` parameter will flush all traces regardless of dirty state
     *
     * @param flushAll Indicate if all data, not just deltas, should be flushed
     */
    public Set<Map.Entry<String, Long>> getDataToFlush(boolean flushAll) {
        for (Map.Entry<String, Long> entry : deltas.entrySet()) {
            setOrIncrementMap(traces, entry.getKey(), entry.getValue());
        }

        ImmutableMap<String, Long> mapData = ImmutableMap.copyOf(flushAll ? traces : deltas);
        Set<Map.Entry<String, Long>> result = mapData.entrySet();
        deltas.clear();
        return result;
    }

    /**
     * Set a new value in a map or increment an existing value
     *
     * @param map The map in which to modify the value
     * @param key The key for the map
     * @param inc The new value or increment for the given key
     */
    private void setOrIncrementMap(Map<String, Long> map, String key, long inc) {
        Long val = map.get(key);
        if (val == null) {
            map.put(key, inc);
        } else {
            map.put(key, val + inc);
        }
    }
}
