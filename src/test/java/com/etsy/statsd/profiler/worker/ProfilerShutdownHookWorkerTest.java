package com.etsy.statsd.profiler.worker;

import com.etsy.statsd.profiler.Profiler;
import com.etsy.statsd.profiler.profilers.MockProfiler2;
import com.etsy.statsd.profiler.profilers.MockProfiler1;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ProfilerShutdownHookWorkerTest {
    @Test
    public void testRunnable() throws InterruptedException {
        Set<String> output = new HashSet<>();
        Profiler mockProfiler1 = new MockProfiler1(output);
        Profiler mockProfiler2 = new MockProfiler2(output);
        Collection<Profiler> profilers = Arrays.asList(mockProfiler1, mockProfiler2);

        Thread t = new Thread(new ProfilerShutdownHookWorker(profilers));
        t.run();
        t.join();

        Set<String> expectedOutput = new HashSet<>();
        expectedOutput.add(MockProfiler1.class.getSimpleName() + "-flushData");
        expectedOutput.add(MockProfiler2.class.getSimpleName() + "-flushData");
        assertEquals(expectedOutput, output);
    }
}