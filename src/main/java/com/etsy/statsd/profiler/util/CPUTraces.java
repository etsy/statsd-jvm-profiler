package com.etsy.statsd.profiler.util;

import com.google.common.collect.Maps;

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
    private int max = Integer.MIN_VALUE;
    private int min = Integer.MAX_VALUE;

    public CPUTraces() {
        traces = new HashMap<>();
    }

    /**
     * Increment the aggregate time for a trace
     *
     * @param traceKey The key for the trace
     * @param inc The value by which to increment the aggregate time for the trace
     */
    public void increment(String traceKey, long inc) {
        MapUtil.setOrIncrementMap(traces, traceKey, inc);
        updateBounds(traceKey);
    }

    /**
     * Get data to be flushed from the state
     * It only returns traces that have been updated since the last flush
     *
     */
    public Map<String, Long> getDataToFlush() {
        Map<String, Long> result = Maps.newHashMap(traces);
        traces.clear();
        return result;
    }

    /**
     * Get the bounds on the number of path components for the CPU trace metrics
     *
     * @return A Pair of integers, the left being the minimum number of components and the right being the maximum
     */
    public Range getBounds() {
        return new Range(min, max);
    }

    private void updateBounds(String traceKey) {
        int numComponents = traceKey.split("\\.").length;
        max = Math.max(max, numComponents);
        min = Math.min(min, numComponents);
    }
}
