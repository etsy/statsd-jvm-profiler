package com.etsy.statsd.profiler.profilers;

import com.etsy.statsd.client.MockStatsDClient;
import org.junit.After;
import org.junit.BeforeClass;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Base class for profiler tests
 * Sets up a fake StatsD listener
 *
 * @author Andrew Johnson
 */
public class ProfilerTestBase {
    protected static MockStatsDClient client;

    @BeforeClass
    public static void setup() {
        client = new MockStatsDClient();
    }

    @After
    public void clearMessages() {
        client.clearMessages();
    }

    protected void assertMessages(Map<String, Long> expected) {
        assertEquals(expected, client.getMessages());
    }

}
