package com.etsy.statsd.client;

import com.timgroup.statsd.ConvenienceMethodProvidingStatsDClient;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Mock StatsD client for use in tests
 * Actual values are not recorded
 *
 * @author Andrew Johnson
 */
public class MockStatsDClient extends ConvenienceMethodProvidingStatsDClient {
    public static final String PREFIX = "mock.";
    public static final String DUMMY = "dummy";

    private Map<String, Long> messages = new HashMap<>();

    @Override
    public void stop() { }

    @Override
    public void count(String aspect, long delta, double sampleRate) {
        record(messageFor(aspect, DUMMY, "c", sampleRate));
    }

    @Override
    public void recordGaugeValue(String aspect, long value) {
        recordGaugeCommon(aspect, DUMMY, value < 0, false);
    }

    @Override
    public void recordGaugeValue(String aspect, double value) {
        recordGaugeCommon(aspect, DUMMY, value < 0, false);
    }

    @Override
    public void recordGaugeDelta(String aspect, long value) {
        recordGaugeCommon(aspect, DUMMY, value < 0, true);
    }

    @Override
    public void recordGaugeDelta(String aspect, double value) {
        recordGaugeCommon(aspect, DUMMY, value < 0, true);
    }

    @Override
    public void recordSetEvent(String aspect, String eventName) {
        record(messageFor(aspect, eventName, "s"));
    }

    @Override
    public void recordExecutionTime(String aspect, long timeInMs, double sampleRate) {
        record(messageFor(aspect, DUMMY, "ms", sampleRate));
    }

    public Map<String, Long> getMessages() {
        return messages;
    }

    public void clearMessages() {
        messages.clear();
    }

    private void recordGaugeCommon(String aspect, String value, boolean negative, boolean delta) {
        final StringBuilder message = new StringBuilder();
        if (!delta && negative) {
            message.append(messageFor(aspect, "0", "g")).append('\n');
        }
        message.append(messageFor(aspect, (delta && !negative) ? ("+" + value) : value, "g"));
        record(message.toString());
    }

    private void record(String message) {
        Long count = messages.get(message);
        if (count == null) {
            messages.put(message, 1L);
        } else {
            messages.put(message, ++count);
        }
    }

    private String messageFor(String aspect, String value, String type) {
        return messageFor(aspect, value, type, 1.0);
    }

    private String messageFor(String aspect, String value, String type, double sampleRate) {
        final String messageFormat = (sampleRate == 1.0) ? "%s%s:%s|%s" : "%s%s:%s|%s@%f";
        return String.format((Locale)null, messageFormat, PREFIX, aspect, value, type, sampleRate);
    }
}
