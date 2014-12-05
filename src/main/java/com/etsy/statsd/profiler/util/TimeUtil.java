package com.etsy.statsd.profiler.util;

import java.util.concurrent.TimeUnit;

/**
 * Utility class for working with time
 *
 * @author Andrew Johnson
 */
public class TimeUtil {
    /**
     * Convert a reporting period into the time scale of a profiling period
     *
     * @param profilePeriod The profiling period
     * @param profileTimeUnit The TimeUnit for the profiling period
     * @param reportingPeriod The reporting period
     * @param reportingTimeUnit The TimeUnit for the reporting period
     * @return The reporting period scaled to the profiling period (i.e. suitable for use like x % convertReportingPeriod(...) == 0)
     */
    public static long convertReportingPeriod(long profilePeriod, TimeUnit profileTimeUnit, long reportingPeriod, TimeUnit reportingTimeUnit) {
        long convertedReportingPeriod = profileTimeUnit.convert(reportingPeriod, reportingTimeUnit);

        // If we profile less frequently than we want report, returning 1 would indicate we should always report
        if (convertedReportingPeriod <= profilePeriod) {
            return 1;
        }

        return convertedReportingPeriod / profilePeriod;
    }
}
