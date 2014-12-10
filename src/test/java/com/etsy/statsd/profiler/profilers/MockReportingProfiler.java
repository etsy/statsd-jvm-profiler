package com.etsy.statsd.profiler.profilers;

import com.etsy.statsd.profiler.Profiler;
import com.etsy.statsd.profiler.reporter.Reporter;

import java.util.concurrent.TimeUnit;

/**
 * Mock profiler for testing
 *
 * @author Andrew Johnson
 */
public class MockReportingProfiler extends Profiler {
    public MockReportingProfiler(Reporter reporter) {
        super(reporter);
    }

    @Override
    public void profile() {
        recordGaugeValue("profile", 1);
    }

    @Override
    public void flushData() {
        recordGaugeValue("flushData", 1);
    }

    @Override
    public long getPeriod() {
        return 0;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return null;
    }
}
