package com.etsy.statsd.profiler.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents the state of the CPU profiler
 *
 * @author Andrew Johnson
 */
public class CPUTraces {
    private Map<String, Long> traces;
    private Set<String> dirtyTraces;

    public CPUTraces() {
        traces = new HashMap<>();
        dirtyTraces = new HashSet<>();
    }

    /**
     * Increment the aggregate time for a trace
     *
     * @param traceKey The key for the trace
     * @param inc The value by which to increment the aggregate time for the trace
     */
    public void increment(String traceKey, long inc) {
        MapUtil.setOrIncrementMap(traces, traceKey, inc);
        dirtyTraces.add(traceKey);
    }

    /**
     * Get data to be flushed from the state
     * By default it only returns traces that have been updated since the last flush
     * But with the `flushAll` parameter will flush all traces regardless of dirty state
     *
     * @param flushAll Indicate if all data, not just deltas, should be flushed
     */
    public Map<String, Long> getDataToFlush(boolean flushAll) {
        Map<String, Long> result = new HashMap<>();
        if (flushAll) {
            result = traces;
        } else {
            for (String trace : dirtyTraces) {
                result.put(trace, traces.get(trace));
            }
        }

        dirtyTraces.clear();
        return result;
    }

    /**
     * Get the bounds on the number of path components for the CPU trace metrics
     *
     * @return A Pair of integers, the left being the minimum number of components and the right being the maximum
     */
    public Range getBounds() {
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;

        for (String key : traces.keySet()) {
            int numComponents = key.split("\\.").length;
            max = Math.max(max, numComponents);
            min = Math.min(min, numComponents);
        }

        return new Range(min, max);
    }
}
