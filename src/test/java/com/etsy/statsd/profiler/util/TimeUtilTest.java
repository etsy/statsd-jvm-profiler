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
    }

    @Test
    public void testIsLargerUnit() {
        assertFalse(TimeUtil.isLargerUnit(TimeUnit.NANOSECONDS, TimeUnit.NANOSECONDS));
        assertTrue(TimeUtil.isLargerUnit(TimeUnit.MICROSECONDS, TimeUnit.NANOSECONDS));
        assertTrue(TimeUtil.isLargerUnit(TimeUnit.MILLISECONDS, TimeUnit.NANOSECONDS));
        assertTrue(TimeUtil.isLargerUnit(TimeUnit.SECONDS, TimeUnit.NANOSECONDS));
        assertTrue(TimeUtil.isLargerUnit(TimeUnit.MINUTES, TimeUnit.NANOSECONDS));
        assertTrue(TimeUtil.isLargerUnit(TimeUnit.HOURS, TimeUnit.NANOSECONDS));
        assertTrue(TimeUtil.isLargerUnit(TimeUnit.DAYS, TimeUnit.NANOSECONDS));

        assertFalse(TimeUtil.isLargerUnit(TimeUnit.NANOSECONDS, TimeUnit.MICROSECONDS));
        assertFalse(TimeUtil.isLargerUnit(TimeUnit.MICROSECONDS, TimeUnit.MICROSECONDS));
        assertTrue(TimeUtil.isLargerUnit(TimeUnit.MILLISECONDS, TimeUnit.MICROSECONDS));
        assertTrue(TimeUtil.isLargerUnit(TimeUnit.SECONDS, TimeUnit.MICROSECONDS));
        assertTrue(TimeUtil.isLargerUnit(TimeUnit.MINUTES, TimeUnit.MICROSECONDS));
        assertTrue(TimeUtil.isLargerUnit(TimeUnit.HOURS, TimeUnit.MICROSECONDS));
        assertTrue(TimeUtil.isLargerUnit(TimeUnit.DAYS, TimeUnit.MICROSECONDS));

        assertFalse(TimeUtil.isLargerUnit(TimeUnit.NANOSECONDS, TimeUnit.MILLISECONDS));
        assertFalse(TimeUtil.isLargerUnit(TimeUnit.MICROSECONDS, TimeUnit.MILLISECONDS));
        assertFalse(TimeUtil.isLargerUnit(TimeUnit.MILLISECONDS, TimeUnit.MILLISECONDS));
        assertTrue(TimeUtil.isLargerUnit(TimeUnit.SECONDS, TimeUnit.MILLISECONDS));
        assertTrue(TimeUtil.isLargerUnit(TimeUnit.MINUTES, TimeUnit.MILLISECONDS));
        assertTrue(TimeUtil.isLargerUnit(TimeUnit.HOURS, TimeUnit.MILLISECONDS));
        assertTrue(TimeUtil.isLargerUnit(TimeUnit.DAYS, TimeUnit.MILLISECONDS));

        assertFalse(TimeUtil.isLargerUnit(TimeUnit.NANOSECONDS, TimeUnit.SECONDS));
        assertFalse(TimeUtil.isLargerUnit(TimeUnit.MICROSECONDS, TimeUnit.SECONDS));
        assertFalse(TimeUtil.isLargerUnit(TimeUnit.MILLISECONDS, TimeUnit.SECONDS));
        assertFalse(TimeUtil.isLargerUnit(TimeUnit.SECONDS, TimeUnit.SECONDS));
        assertTrue(TimeUtil.isLargerUnit(TimeUnit.MINUTES, TimeUnit.SECONDS));
        assertTrue(TimeUtil.isLargerUnit(TimeUnit.HOURS, TimeUnit.SECONDS));
        assertTrue(TimeUtil.isLargerUnit(TimeUnit.DAYS, TimeUnit.SECONDS));

        assertFalse(TimeUtil.isLargerUnit(TimeUnit.NANOSECONDS, TimeUnit.MINUTES));
        assertFalse(TimeUtil.isLargerUnit(TimeUnit.MICROSECONDS, TimeUnit.MINUTES));
        assertFalse(TimeUtil.isLargerUnit(TimeUnit.MILLISECONDS, TimeUnit.MINUTES));
        assertFalse(TimeUtil.isLargerUnit(TimeUnit.SECONDS, TimeUnit.MINUTES));
        assertFalse(TimeUtil.isLargerUnit(TimeUnit.MINUTES, TimeUnit.MINUTES));
        assertTrue(TimeUtil.isLargerUnit(TimeUnit.HOURS, TimeUnit.MINUTES));
        assertTrue(TimeUtil.isLargerUnit(TimeUnit.DAYS, TimeUnit.MINUTES));

        assertFalse(TimeUtil.isLargerUnit(TimeUnit.NANOSECONDS, TimeUnit.HOURS));
        assertFalse(TimeUtil.isLargerUnit(TimeUnit.MICROSECONDS, TimeUnit.HOURS));
        assertFalse(TimeUtil.isLargerUnit(TimeUnit.MILLISECONDS, TimeUnit.HOURS));
        assertFalse(TimeUtil.isLargerUnit(TimeUnit.SECONDS, TimeUnit.HOURS));
        assertFalse(TimeUtil.isLargerUnit(TimeUnit.MINUTES, TimeUnit.HOURS));
        assertFalse(TimeUtil.isLargerUnit(TimeUnit.HOURS, TimeUnit.HOURS));
        assertTrue(TimeUtil.isLargerUnit(TimeUnit.DAYS, TimeUnit.HOURS));

        assertFalse(TimeUtil.isLargerUnit(TimeUnit.NANOSECONDS, TimeUnit.DAYS));
        assertFalse(TimeUtil.isLargerUnit(TimeUnit.MICROSECONDS, TimeUnit.DAYS));
        assertFalse(TimeUtil.isLargerUnit(TimeUnit.MILLISECONDS, TimeUnit.DAYS));
        assertFalse(TimeUtil.isLargerUnit(TimeUnit.SECONDS, TimeUnit.DAYS));
        assertFalse(TimeUtil.isLargerUnit(TimeUnit.MINUTES, TimeUnit.DAYS));
        assertFalse(TimeUtil.isLargerUnit(TimeUnit.HOURS, TimeUnit.DAYS));
        assertFalse(TimeUtil.isLargerUnit(TimeUnit.DAYS, TimeUnit.DAYS));
    }
}