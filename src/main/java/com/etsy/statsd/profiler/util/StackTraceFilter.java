package com.etsy.statsd.profiler.util;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for filtering stack traces
 * Assumes a string representation like that produced by @link{StackTraceFormatter}
 *
 * @author Andrew Johnson
 */
public class StackTraceFilter {
    public static final Pattern MATCH_EVERYTHING = Pattern.compile("^.*$");
    public static final Pattern MATCH_NOTHING = Pattern.compile("$^");

    private Pattern includePattern;
    private Pattern excludePattern;

    public StackTraceFilter(List<String> includePackages, List<String> excludePackages) {
        includePattern = getPackagePattern(includePackages, MATCH_EVERYTHING);
        excludePattern = getPackagePattern(excludePackages, MATCH_NOTHING);
    }

    /**
     * Indicate if this stack trace should be included in the filter
     * Checks if the stack trace matches the include pattern and does not match the exclude pattern
     *
     * @param formattedStackTrace The stack trace to check for inclusion in the filter
     * @return True if it should be included, false otherwise
     */
    public boolean includeStackTrace(String formattedStackTrace) {
        return includeMatches(formattedStackTrace) && !excludeMatches(formattedStackTrace);
    }

    /**
     * Indicate if this stack trace matches one of the included packages
     *
     * @param formattedStackTrace The stack trace to check against the included packages
     * @return True if it matches an included package, false otherwise
     */
    public boolean includeMatches(String formattedStackTrace) {
        return matches(includePattern, formattedStackTrace);
    }

    /**
     * Indicate if this stack trace matches one of the excluded packages
     *
     * @param formattedStackTrace The stack trace to check against the excluded packages
     * @return True if it matches an excluded package, false otherwise
     */
    public boolean excludeMatches(String formattedStackTrace) {
        return matches(excludePattern, formattedStackTrace);
    }

    /**
     * Indicate if this stack trace matches the given pattern
     *
     * @param pattern The pattern to match against the stack trace
     * @param formattedStackTrace The stack trace to check against the pattern
     * @return True if the stack trace matches the pattern, false otherwise
     */
    private boolean matches(Pattern pattern, String formattedStackTrace) {
        Matcher matcher = pattern.matcher(formattedStackTrace);

        return matcher.matches();
    }

    /**
     * Construct a Pattern that matches any of the given packages
     *
     * @param packageWhitelist The packages to match in this Pattern
     * @return A Pattern object that matches any of the given packages
     */
    public Pattern getPackagePattern(List<String> packageWhitelist, Pattern defaultPattern) {
        if (packageWhitelist == null || packageWhitelist.isEmpty()) {
            return defaultPattern;
        }
        else {
            return Pattern.compile(String.format("(.*\\.|^)(%s).*",
                    Joiner.on("|").join(Lists.transform(packageWhitelist, new Function<String, String>() {
                        @Override
                        public String apply(String s) {
                            return String.format("(%s)", s.replace(".", "-"));
                        }
                    }))));
        }
    }
}
