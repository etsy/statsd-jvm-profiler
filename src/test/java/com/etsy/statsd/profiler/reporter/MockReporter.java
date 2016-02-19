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
    private Map<String, Number> output;

    public MockReporter() {
        super(MockArguments.BASIC);
        output = new HashMap<>();
    }

    @Override
    public void recordGaugeValue(String key, long value) {
        MapUtil.setOrIncrementMap(output, key, value);
    }

    @Override
    public void recordGaugeValues(Map<String, ? extends Number> gauges) {
        for (Map.Entry<String, ? extends Number> gauge : gauges.entrySet()) {
            if (gauge.getValue() instanceof Long) {
                recordGaugeValue(gauge.getKey(), gauge.getValue().longValue());
            } else if (gauge.getValue() instanceof Double) {
                recordGaugeValue(gauge.getKey(), gauge.getValue().doubleValue());
            } else {
                throw new IllegalArgumentException("Unexpected Number type: " + gauge.getValue().getClass().getSimpleName());
            }
        }
    }

    @Override
    public void recordGaugeValue(String key, double value) {
        MapUtil.setOrIncrementMap(output, key, value);
    }

    @Override
    protected String createClient(String server, int port, String prefix) {
        return "";
    }

    @Override
    protected void handleArguments(Arguments arguments) { }

    public Map<String, Number> getOutput() {
        return output;
    }
}
