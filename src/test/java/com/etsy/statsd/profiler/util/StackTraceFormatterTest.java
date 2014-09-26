package com.etsy.statsd.profiler.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class StackTraceFormatterTest {
    @Test
    public void testFormatStackTraceElement() {
        StackTraceElement element = new StackTraceElement("com.etsy.statsd.profiler.util.StackTraceFormatter",
                "formatStackTraceElement", "StackTraceFormatter.java", 21);
        StackTraceElement elementNoFile = new StackTraceElement("com.etsy.statsd.profiler.util.StackTraceFormatter",
                "formatStackTraceElement", null, 0);

        String expected = "com-etsy-statsd-profiler-util-StackTraceFormatter-formatStackTraceElement";

        assertEquals(expected, StackTraceFormatter.formatStackTraceElement(element));
        assertEquals(expected, StackTraceFormatter.formatStackTraceElement(elementNoFile));
    }

    @Test
    public void testFormatEmptyStackTrace() {
        StackTraceElement[] stack = new StackTraceElement[0];

        assertEquals("", StackTraceFormatter.formatStackTrace(stack));
    }

    @Test
    public void testFormatStackTrace() {
        StackTraceElement[] stack = new StackTraceElement[2];
        stack[0] = new StackTraceElement("com.etsy.statsd.profiler.util.StackTraceFormatter",
                "formatStackTraceElement", "StackTraceFormatter.java", 21);
        stack[1] = new StackTraceElement("com.etsy.statsd.profiler.util.StackTraceFormatterTest",
                "testFormatStackTraceElement", "StackTraceFormatterTest.java", 17);

        String expected = "com-etsy-statsd-profiler-util-StackTraceFormatter-formatStackTraceElement.com-etsy-statsd-profiler-util-StackTraceFormatterTest-testFormatStackTraceElement";

        assertEquals(expected, StackTraceFormatter.formatStackTrace(stack));
    }
}