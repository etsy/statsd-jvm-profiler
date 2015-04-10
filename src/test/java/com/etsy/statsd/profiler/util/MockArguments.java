package com.etsy.statsd.profiler.util;

import com.etsy.statsd.profiler.Arguments;

import java.util.Map;

/**
 * Utility class to create mock arguments for testing
 */
public class MockArguments {
    public static final Arguments BASIC = createArgs("localhost", 8888, "statsd-jvm-profiler", null);

    /**
     * Create an Arguments instance for testing
     *
     * @param server The server argument
     * @param port The port argument
     * @param prefix The prefix argument
     * @param otherArgs Any additional arguments to include
     * @return An Arguments instance containing all the given arguments
     */
    public static Arguments createArgs(String server, int port, String prefix, Map<String, String> otherArgs) {
        String args = String.format("server=%s,port=%d,prefix=%s", server, port, prefix);
        if (otherArgs != null) {
            for (Map.Entry<String, String> entry : otherArgs.entrySet()) {
                args += String.format(",%s=%s", entry.getKey(), entry.getValue());
            }
        }

        return Arguments.parseArgs(args);
    }
}
