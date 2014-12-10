package com.etsy.statsd.profiler.util;

import com.etsy.statsd.profiler.profilers.CPUProfiler;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class StackTraceFilterTest {
    private static StackTraceFilter filter;
    private static List<String> includePackages;
    private static List<String> excludedTraces;
    private static List<String> includedTraces;
    private static List<String> otherTraces;

    @BeforeClass
    public static void setup() {
        includePackages = Arrays.asList("com.etsy", "com.twitter.scalding");
        filter = new StackTraceFilter(includePackages, CPUProfiler.EXCLUDE_PACKAGES);
        excludedTraces = Arrays.asList("com-etsy-statsd-profiler-profiler-util-StackTraceFormatter-formatStackTraceElement", "com-timgroup-statsd-StatsDClient-send", "com-etsy-statsd-profiler-profiler-util-StackTraceFormatter-formatStackTraceElement.com-etsy-Foo-fooTest");
        includedTraces = Arrays.asList("com-etsy-foo-fooTest");
        otherTraces = Arrays.asList("com-google-guava-Foo-helloWorld");
    }

    @Test
    public void testGetPackagePattern() {
        Pattern expected = Pattern.compile("(.*\\.|^)((com-etsy)|(com-twitter-scalding)).*");
        assertEquals(expected.toString(), filter.getPackagePattern(includePackages, StackTraceFilter.MATCH_EVERYTHING).toString());

        assertEquals(StackTraceFilter.MATCH_EVERYTHING.toString(), filter.getPackagePattern(null, StackTraceFilter.MATCH_EVERYTHING).toString());
        assertEquals(StackTraceFilter.MATCH_EVERYTHING.toString(), filter.getPackagePattern(new ArrayList<String>(), StackTraceFilter.MATCH_EVERYTHING).toString());
    }

    @Test
    public void testExcludeMatches() {
        for (String e : excludedTraces) {
            assertTrue(e, filter.excludeMatches(e));
        }

        for (String i : includedTraces) {
            assertFalse(i, filter.excludeMatches(i));
        }

        for (String o : otherTraces) {
            assertFalse(o, filter.excludeMatches(o));
        }
    }

    @Test
    public void testIncludeMatches() {
        for (String i : includedTraces) {
            assertTrue(i, filter.includeMatches(i));
        }

        for (String o : otherTraces) {
            assertFalse(o, filter.includeMatches(o));
        }
    }

    @Test
    public void testIncludeStackTrace() {
        for (String e : excludedTraces) {
            assertFalse(e, filter.includeStackTrace(e));
        }

        for (String i : includedTraces) {
            assertTrue(i, filter.includeStackTrace(i));
        }

        for (String o : otherTraces) {
            assertFalse(o, filter.includeStackTrace(o));
        }
    }
}