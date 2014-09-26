package com.etsy.statsd.profiler;

import com.google.common.base.Optional;

import java.util.*;

/**
 * Represents arguments to the profiler
 *
 * @author Andrew Johnson
 */
public class Arguments {
    private static final String STATSD_SERVER = "server";
    private static final String STATSD_PORT = "port";
    private static final String METRICS_PREFIX = "prefix";
    private static final String FILTER_PACKAGES = "filterPackages";

    private static final Collection<String> REQUIRED = Arrays.asList(STATSD_SERVER, STATSD_PORT);

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

    public String statsdServer;
    public int statsdPort;
    public Optional<String> metricsPrefix;
    public Optional<List<String>> filterPackages;

    private Arguments(Map<String, String> parsedArgs) {
        statsdServer = parsedArgs.get(STATSD_SERVER);
        statsdPort = Integer.parseInt(parsedArgs.get(STATSD_PORT));
        metricsPrefix = Optional.fromNullable(parsedArgs.get(METRICS_PREFIX));

        String packages = parsedArgs.get(FILTER_PACKAGES);
        if (packages == null) {
            filterPackages = Optional.absent();
        }
        else {
            filterPackages = Optional.of(Arrays.asList(packages.split(":")));
        }
    }
}
