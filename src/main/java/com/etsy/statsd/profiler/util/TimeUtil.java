package com.etsy.statsd.profiler.util;

import java.util.concurrent.TimeUnit;

/**
 * Utility class for working with time
 *
 * @author Andrew Johnson
 */
public class TimeUtil {
    /**
     * Determine if one time unit is larger than another
     *
     * @param standard The TimeUnit for comparison
     * @param toCompare The TimeUnit to compare against `standard`
     * @return true if standard is a larger unit that toCompare, false otherwise
     */
    public static boolean isLargerUnit(TimeUnit standard, TimeUnit toCompare) {
        switch (toCompare) {
            case NANOSECONDS:
                return standard != TimeUnit.NANOSECONDS;
            case MICROSECONDS:
                return standard != TimeUnit.MICROSECONDS && standard != TimeUnit.NANOSECONDS;
            case MILLISECONDS:
                return standard != TimeUnit.MICROSECONDS && standard != TimeUnit.NANOSECONDS
                        && standard != TimeUnit.MILLISECONDS;
            case SECONDS:
                return standard == TimeUnit.MINUTES || standard == TimeUnit.HOURS || standard == TimeUnit.DAYS;
            case MINUTES:
                return standard == TimeUnit.HOURS || standard == TimeUnit.DAYS;
            case HOURS:
                return standard == TimeUnit.DAYS;
            case DAYS:
                return false;
            default:
                throw new IllegalArgumentException("Unknown TimeUnit " + toCompare);
        }
    }

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
