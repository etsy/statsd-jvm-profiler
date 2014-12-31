package com.etsy.statsd.profiler.util;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for formatting stack traces
 *
 * @author Andrew Johnson
 */
public class StackTraceFormatter {
    /**
     * Formats a StackTraceElement as a String, excluding the line number
     *
     * @param element The StackTraceElement to format
     * @return A String representing the given StackTraceElement
     */
    public static String formatStackTraceElement(StackTraceElement element) {
        return String.format("%s-%s", element.getClassName().replace(".", "-"), element.getMethodName());
    }

    /**
     * Formats an entire stack trace as a String
     *
     * @param stack The stack trace to format
     * @return A String representing the given stack trace
     */
    public static String formatStackTrace(StackTraceElement[] stack) {
        List<String> lines = new ArrayList<>();
        for (StackTraceElement element : stack) {
            lines.add(formatStackTraceElement(element));
        }

        return Joiner.on(".").join(lines);
    }
}
