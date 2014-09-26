package com.etsy.statsd.profiler.worker;

import com.etsy.statsd.profiler.Profiler;
import com.etsy.statsd.profiler.profilers.MockProfiler1;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class ProfilerWorkerThreadTest {
    @Test
    public void testRunnable() throws InterruptedException {
        Set<String> output = new HashSet<>();
        Profiler mockProfiler1 = new MockProfiler1(output);

        Thread t = new Thread(new ProfilerWorkerThread(mockProfiler1));
        t.run();
        t.join();

        Set<String> expectedOutput = new HashSet<>();
        expectedOutput.add(MockProfiler1.class.getSimpleName() + "-profile");
        assertEquals(expectedOutput, output);
    }
}