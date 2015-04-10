package com.etsy.statsd.profiler.reporter.mock;

import com.etsy.statsd.profiler.reporter.Reporter;
import com.google.common.base.Function;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

/**
 * Base class for Reporter tests
 */
public abstract class BaseReporterTest<T extends Reporter<?>> {
    @InjectMocks
    protected T reporter = constructReporter();

    private Function<Object[], Void> testCaseFunction = new Function<Object[], Void>() {
        @Override
        public Void apply(Object[] input) {
            testCase(input);
            return null;
        }
    };

    protected Answer answer = new ReporterAnswer(testCaseFunction);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    protected abstract T constructReporter();

    protected abstract void testCase(Object[] args);
}
