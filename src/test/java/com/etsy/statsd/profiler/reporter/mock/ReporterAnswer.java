package com.etsy.statsd.profiler.reporter.mock;

import com.google.common.base.Function;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mockito Answer for use testing reporters
 */
public class ReporterAnswer implements Answer {
    private Function<Object[], Void> testCase;

    public ReporterAnswer(Function<Object[], Void> testCase) {
        this.testCase = testCase;
    }

    @Override
    public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
        Object[] args = invocationOnMock.getArguments();
        testCase.apply(args);

        return null;
    }
}
