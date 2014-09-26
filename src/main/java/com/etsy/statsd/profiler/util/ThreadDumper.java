package com.etsy.statsd.profiler.util;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.Collection;

/**
 * Dumps current thread state
 *
 * @author Andrew Johnson
 */
public class ThreadDumper {
    private static ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    /**
     * Predicate to filter by thread state
     */
    private static class ThreadStatePredicate implements Predicate<ThreadInfo> {
        private Thread.State state;

        public ThreadStatePredicate(Thread.State state) {
            this.state = state;
        }

        @Override
        public boolean apply(ThreadInfo input) {
            return input.getThreadState() == state;
        }
    }

    /**
     * Dump state of all threads
     *
     * @param lockedMonitors      If true, dump all locked monitors
     * @param lockedSynchronizers If true, dump all locked ownable synchronizers
     * @return A Collection of {@link ThreadInfo} for all live threads
     */
    public static Collection<ThreadInfo> getAllThreads(boolean lockedMonitors, boolean lockedSynchronizers) {
        return Arrays.asList(threadMXBean.dumpAllThreads(lockedMonitors, lockedSynchronizers));
    }

    /**
     * Dump state of all threads with a given state
     *
     * @param lockedMonitors      If true, dump all locked monitors
     * @param lockedSynchronizers If true, dump all locked ownable synchronizers
     * @param state               The state in which a thread must be to be dumped
     * @return A Collection of {@link ThreadInfo} for all live threads in the given state
     */
    public static Collection<ThreadInfo> getAllThreadsInState(boolean lockedMonitors, boolean lockedSynchronizers, Thread.State state) {
        return Collections2.filter(getAllThreads(lockedMonitors, lockedSynchronizers), new ThreadStatePredicate(state));
    }

    /**
     * Dump state of all threads with a given state that match a predicate
     *
     * @param lockedMonitors      If true, dump all locked monitors
     * @param lockedSynchronizers If true, dump all locked ownable synchronizers
     * @param state               The state in which a thread must be to be dumped
     * @param threadInfoPredicate Predicate to further filter the dumped threads
     * @return A Collection of {@link ThreadInfo} for all live threads in the given state that match the given predicate
     */
    public static Collection<ThreadInfo> filterAllThreadsInState(boolean lockedMonitors, boolean lockedSynchronizers, Thread.State state, Predicate<ThreadInfo> threadInfoPredicate) {
        return Collections2.filter(getAllThreadsInState(lockedMonitors, lockedSynchronizers, state), threadInfoPredicate);
    }
}

