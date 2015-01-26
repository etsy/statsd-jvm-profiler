package com.etsy.statsd.profiler;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class ArgumentsTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testInvalidArgument() {
        String args = "key=value,key2";

        exception.expect(IllegalArgumentException.class);
        Arguments.parseArgs(args);
    }

    @Test
    public void testMissingRequiredArgument() {
        String args = "server=localhost,prefix=prefix";

        exception.expect(IllegalArgumentException.class);
        Arguments.parseArgs(args);
    }

    @Test
    public void testNonNumericPort() {
        String args = "server=localhost,port=abcd";

        exception.expect(NumberFormatException.class);
        Arguments.parseArgs(args);
    }

    @Test
    public void testNoOptionalArguments() {
        String args = "server=localhost,port=8125";
        Arguments arguments = Arguments.parseArgs(args);

        assertEquals("localhost", arguments.statsdServer);
        assertEquals(8125, arguments.statsdPort);
        assertEquals("default", arguments.metricsPrefix.or("default"));
    }

    @Test
    public void testOptionalArguments() {
        String args = "server=localhost,port=8125,prefix=i.am.a.prefix,packageWhitelist=com.etsy";
        Arguments arguments = Arguments.parseArgs(args);

        assertEquals("localhost", arguments.statsdServer);
        assertEquals(8125, arguments.statsdPort);
        assertEquals("i.am.a.prefix", arguments.metricsPrefix.or("default"));
    }
}