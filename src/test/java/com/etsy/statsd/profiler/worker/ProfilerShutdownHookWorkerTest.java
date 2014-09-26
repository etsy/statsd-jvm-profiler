package com.etsy.statsd.profiler.worker;

import com.etsy.statsd.profiler.Profiler;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

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
        expectedOutput.add(MockProfiler1.class.getSimpleName());
        expectedOutput.add(MockProfiler2.class.getSimpleName());
        assertEquals(expectedOutput, output);
    }

    private static class MockProfiler2 extends Profiler {
        private Set<String> output;

        public MockProfiler2(Set<String> output) {
            super(null);
            this.output = output;
        }

        @Override
        public void profile() { }

        @Override
        public void flushData() {
            output.add(this.getClass().getSimpleName());
        }

        @Override
        public long getPeriod() {
            return 0;
        }

        @Override
        public TimeUnit getTimeUnit() {
            return null;
        }
    }

    private static class MockProfiler1 extends Profiler {
        private Set<String> output;

        public MockProfiler1(Set<String> output) {
            super(null);
            this.output = output;
        }

        @Override
        public void profile() { }

        @Override
        public void flushData() {
            output.add(this.getClass().getSimpleName());
        }

        @Override
        public long getPeriod() {
            return 0;
        }

        @Override
        public TimeUnit getTimeUnit() {
            return null;
        }
    }
}