package com.etsy.statsd.profiler.reporter;

import com.etsy.statsd.profiler.reporter.mock.BaseReporterTest;
import com.etsy.statsd.profiler.util.MockArguments;
import com.timgroup.statsd.StatsDClient;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

public class StatsDReporterTest extends BaseReporterTest<StatsDReporter> {
    @Mock
    private StatsDClient client;

    @Override
    protected StatsDReporter constructReporter() {
        return new StatsDReporter(MockArguments.BASIC);
    }

    @Override
    protected void testCase(Object[] args) {
        assertEquals(2, args.length);
        assertEquals("fake", args[0]);
        assertEquals(100L, args[1]);
    }

    @Test
    public void testRecordGaugeValue() {
        Mockito.doAnswer(answer).when(client).recordGaugeValue(Matchers.anyString(), Matchers.anyLong());
        reporter.recordGaugeValue("fake", 100L);
    }
}