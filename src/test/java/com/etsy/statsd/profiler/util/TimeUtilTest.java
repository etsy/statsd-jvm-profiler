package com.etsy.statsd.profiler.util;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class TimeUtilTest {
    @Test
    public void testConvertReportingPeriod() {
        assertEquals(1, TimeUtil.convertReportingPeriod(1, TimeUnit.MILLISECONDS, 100, TimeUnit.MICROSECONDS));
        assertEquals(10000, TimeUtil.convertReportingPeriod(1, TimeUnit.MILLISECONDS, 10, TimeUnit.SECONDS));
        assertEquals(1000, TimeUtil.convertReportingPeriod(10, TimeUnit.MILLISECONDS, 10, TimeUnit.SECONDS));
        assertEquals(100000, TimeUtil.convertReportingPeriod(100, TimeUnit.MICROSECONDS, 10, TimeUnit.SECONDS));
    }
}