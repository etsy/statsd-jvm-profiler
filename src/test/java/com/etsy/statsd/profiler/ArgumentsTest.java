package com.etsy.statsd.profiler;

import com.etsy.statsd.profiler.profilers.CPUProfiler;
import com.etsy.statsd.profiler.profilers.MemoryProfiler;
import com.etsy.statsd.profiler.reporter.InfluxDBReporter;
import com.etsy.statsd.profiler.reporter.StatsDReporter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashSet;
import java.util.Set;

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

        assertEquals("localhost", arguments.server);
        assertEquals(8125, arguments.port);
        assertEquals("statsd-jvm-profiler", arguments.metricsPrefix);
    }

    @Test
    public void testOptionalArguments() {
        String args = "server=localhost,port=8125,prefix=i.am.a.prefix,packageWhitelist=com.etsy";
        Arguments arguments = Arguments.parseArgs(args);

        assertEquals("localhost", arguments.server);
        assertEquals(8125, arguments.port);
        assertEquals("i.am.a.prefix", arguments.metricsPrefix);
    }

    @Test
    public void testDefaultProfilers() {
        String args = "server=localhost,port=8125";
        Arguments arguments = Arguments.parseArgs(args);

        Set<Class<? extends Profiler>> expected = new HashSet<>();
        expected.add(CPUProfiler.class);
        expected.add(MemoryProfiler.class);

        assertEquals(expected, arguments.profilers);
    }

    @Test
    public void testProfilerWithPackage() {
        String args = "server=localhost,port=8125,profilers=com.etsy.statsd.profiler.profilers.CPUProfiler";
        Arguments arguments = Arguments.parseArgs(args);

        Set<Class<? extends Profiler>> expected = new HashSet<>();
        expected.add(CPUProfiler.class);

        assertEquals(expected, arguments.profilers);
    }

    @Test
    public void testProfilerWithoutPackage() {
        String args = "server=localhost,port=8125,profilers=MemoryProfiler";
        Arguments arguments = Arguments.parseArgs(args);

        Set<Class<? extends Profiler>> expected = new HashSet<>();
        expected.add(MemoryProfiler.class);

        assertEquals(expected, arguments.profilers);
    }

    @Test
    public void testMultipleProfilers() {
        String args = "server=localhost,port=8125,profilers=CPUProfiler:MemoryProfiler";
        Arguments arguments = Arguments.parseArgs(args);

        Set<Class<? extends Profiler>> expected = new HashSet<>();
        expected.add(CPUProfiler.class);
        expected.add(MemoryProfiler.class);

        assertEquals(expected, arguments.profilers);
    }

    @Test
    public void testProfilerNotFound() {
        String args = "server=localhost,port=8125,profilers=FakeProfiler";

        exception.expect(IllegalArgumentException.class);
        Arguments.parseArgs(args);
    }

    @Test
    public void testReporterWithoutPackage() {
        String args = "server=localhost,port=8125,reporter=InfluxDBReporter";
        Arguments arguments = Arguments.parseArgs(args);

        assertEquals(InfluxDBReporter.class, arguments.reporter);
    }

    @Test
    public void testReporterWithPackage() {
        String args = "server=localhost,port=8125,reporter=com.etsy.statsd.profiler.reporter.InfluxDBReporter";
        Arguments arguments = Arguments.parseArgs(args);

        assertEquals(InfluxDBReporter.class, arguments.reporter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReporterNotFound() {
        String args = "server=localhost,port=8125,reporter=NotRealReporter";
        Arguments.parseArgs(args);
    }

    @Test
    public void testDefaultReporter() {
        String args = "server=localhost,port=8125";
        Arguments arguments = Arguments.parseArgs(args);

        assertEquals(StatsDReporter.class, arguments.reporter);
    }
}