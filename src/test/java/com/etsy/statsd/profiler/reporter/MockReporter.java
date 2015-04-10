package com.etsy.statsd.profiler.reporter;

import com.etsy.statsd.profiler.Arguments;
import com.etsy.statsd.profiler.util.MapUtil;
import com.etsy.statsd.profiler.util.MockArguments;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock reporter for testing
 *
 * @author Andrew Johnson
 */
public class MockReporter extends Reporter<String> {
    private Map<String, Long> output;

    public MockReporter() {
        super(MockArguments.BASIC);
        output = new HashMap<>();
    }

    @Override
    public void recordGaugeValue(String key, long value) {
        MapUtil.setOrIncrementMap(output, key, value);
    }

    @Override
    protected String createClient(String server, int port, String prefix) {
        return "";
    }

    @Override
    protected void handleArguments(Arguments arguments) { }

    public Map<String, Long> getOutput() {
        return output;
    }
}
