package com.etsy.statsd.profiler;

import com.etsy.statsd.profiler.profilers.MockProfilerWithArguments;
import com.etsy.statsd.profiler.reporter.MockReporter;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class ProfilerTest {
    @Test
    public void testHandleArguments() {
        Arguments arguments = Arguments.parseArgs("server=hostname,port=1234,fakeArg=notreal");
        MockProfilerWithArguments profiler = new MockProfilerWithArguments(new MockReporter(), arguments);
        assertEquals("notreal", profiler.fake);
    }

    @Test(expected = NullPointerException.class)
    public void testNullReporterArg() {
        new Profiler(null, null) {
            @Override
            public void profile() { }

            @Override
            public void flushData() { }

            @Override
            public long getPeriod() {
                return 0;
            }

            @Override
            public TimeUnit getTimeUnit() {
                return null;
            }

            @Override
            protected void handleArguments(Arguments arguments) { }
        };
    }
}
