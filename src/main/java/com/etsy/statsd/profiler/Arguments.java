package com.etsy.statsd.profiler;

import com.etsy.statsd.profiler.profilers.CPUProfiler;
import com.etsy.statsd.profiler.profilers.MemoryProfiler;
import com.etsy.statsd.profiler.reporter.Reporter;
import com.etsy.statsd.profiler.reporter.StatsDReporter;
import com.google.common.base.Optional;

import java.util.*;

/**
 * Represents arguments to the profiler
 *
 * @author Andrew Johnson
 */
public class Arguments {
    private static final String SERVER = "server";
    private static final String PORT = "port";
    private static final String METRICS_PREFIX = "prefix";
    private static final String PROFILERS = "profilers";
    private static final String REPORTER = "reporter";

    private static final Collection<String> REQUIRED = Arrays.asList(SERVER, PORT);

    /**
     * Parses arguments into an Arguments object
     *
     * @param args A String containing comma-delimited args in k=v form
     * @return An Arguments object representing the given arguments
     */
    public static Arguments parseArgs(final String args) {
        Map<String, String> parsed = new HashMap<>();
        for (String argPair : args.split(",")) {
            String[] tokens = argPair.split("=");
            if (tokens.length != 2) {
                throw new IllegalArgumentException("statsd-jvm-profiler takes a comma-delimited list of arguments in k=v form");
            }

            parsed.put(tokens[0], tokens[1]);
        }

        for (String requiredArg : REQUIRED) {
            if (!parsed.containsKey(requiredArg)) {
                throw new IllegalArgumentException(String.format("%s argument was not supplied", requiredArg));
            }
        }

        return new Arguments(parsed);
    }

    public String server;
    public int port;
    public String metricsPrefix;
    public Set<Class<? extends Profiler>> profilers;
    public Map<String, String> remainingArgs;
    public Class<? extends Reporter<?>> reporter;

    private Arguments(Map<String, String> parsedArgs) {
        server = parsedArgs.get(SERVER);
        port = Integer.parseInt(parsedArgs.get(PORT));
        metricsPrefix = Optional.fromNullable(parsedArgs.get(METRICS_PREFIX)).or("statsd-jvm-profiler");
        profilers = parseProfilerArg(parsedArgs.get(PROFILERS));
        reporter = parserReporterArg(parsedArgs.get(REPORTER));

        parsedArgs.remove(SERVER);
        parsedArgs.remove(PORT);
        parsedArgs.remove(METRICS_PREFIX);
        parsedArgs.remove(PROFILERS);
        remainingArgs = parsedArgs;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Reporter<?>> parserReporterArg(String reporterArg) {
        if (reporterArg == null) {
            return StatsDReporter.class;
        } else {
            try {
                return (Class<? extends Reporter<?>>) Class.forName(reporterArg);
            } catch (ClassNotFoundException e) {
                // This might indicate the package was left off, so we'll try with the default package
                try {
                    return (Class<? extends Reporter<?>>) Class.forName("com.etsy.statsd.profiler.reporter." + reporterArg);
                } catch (ClassNotFoundException inner) {
                    throw new IllegalArgumentException("Reporter " + reporterArg + " not found", inner);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Set<Class<? extends Profiler>> parseProfilerArg(String profilerArg) {
        Set<Class<? extends Profiler>> profilers = new HashSet<>();
        if (profilerArg == null) {
            profilers.add(CPUProfiler.class);
            profilers.add(MemoryProfiler.class);
        } else {
            for (String p : profilerArg.split(":")) {
                try {
                    profilers.add((Class<? extends Profiler>) Class.forName(p));
                } catch (ClassNotFoundException e) {
                    // This might indicate the package was left off, so we'll try with the default package
                    try {
                        profilers.add((Class<? extends Profiler>) Class.forName("com.etsy.statsd.profiler.profilers." + p));
                    } catch (ClassNotFoundException inner) {
                        throw new IllegalArgumentException("Profiler " + p + " not found", inner);
                    }
                }
            }
        }

        if (profilers.isEmpty()) {
            throw new IllegalArgumentException("At least one profiler must be run");
        }

        return profilers;
    }
}
