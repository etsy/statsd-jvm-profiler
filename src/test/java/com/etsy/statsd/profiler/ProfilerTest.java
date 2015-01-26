package com.etsy.statsd.profiler;

import com.etsy.statsd.profiler.profilers.MockProfilerWithArguments;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProfilerTest {
    @Test
    public void testHandleArguments() {
        Arguments arguments = Arguments.parseArgs("server=hostname,port=1234,fakeArg=notreal");
        MockProfilerWithArguments profiler = new MockProfilerWithArguments(null, arguments);
        assertEquals("notreal", profiler.fake);
    }
}
