package com.etsy.statsd.profiler.reporter;

import com.etsy.statsd.profiler.util.MapUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock reporter for testing
 *
 * @author Andrew Johnson
 */
public class MockReporter implements Reporter {
    private Map<String, Long> output;

    public MockReporter() {
        output = new HashMap<>();
    }

    @Override
    public void recordGaugeValue(String key, long value) {
        MapUtil.setOrIncrementMap(output, key, value);
    }

    public Map<String, Long> getOutput() {
        return output;
    }
}
