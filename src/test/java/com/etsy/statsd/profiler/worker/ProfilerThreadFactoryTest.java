package com.etsy.statsd.profiler.worker;

import org.junit.Test;

import static org.junit.Assert.*;

public class ProfilerThreadFactoryTest {
    @Test
    public void testThreadName() {
        ProfilerThreadFactory factory = new ProfilerThreadFactory();
        Thread t = factory.newThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("dummy");
            }
        });

        assertTrue(t.getName().startsWith(ProfilerThreadFactory.NAME_PREFIX));
    }
}