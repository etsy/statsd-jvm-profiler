package com.etsy.statsd.profiler;

import com.etsy.statsd.profiler.reporter.Reporter;
import com.etsy.statsd.profiler.reporter.StatsDReporter;
import com.etsy.statsd.profiler.worker.ProfilerShutdownHookWorker;
import com.etsy.statsd.profiler.worker.ProfilerThreadFactory;
import com.etsy.statsd.profiler.worker.ProfilerWorkerThread;
import com.google.common.util.concurrent.MoreExecutors;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * javaagent profiler using StatsD as a backend
 *
 * @author Andrew Johnson
 */
public class Agent {
    public static final int EXECUTOR_DELAY = 0;

    /**
     * Start the profiler
     *
     * @param args Profiler arguments
     * @param instrumentation Instrumentation agent
     */
    public static void premain(final String args, final Instrumentation instrumentation) {
        Arguments arguments = Arguments.parseArgs(args);
        String statsdServer = arguments.statsdServer;
        int statsdPort = arguments.statsdPort;
        String prefix = arguments.metricsPrefix.or("statsd-jvm-profiler");

        Reporter reporter = new StatsDReporter(statsdServer, statsdPort, prefix);

        Collection<Profiler> profilers = new ArrayList<Profiler>();
        for (Class<? extends Profiler> profiler : arguments.profilers) {
            try {
                Constructor<? extends Profiler> constructor = profiler.getConstructor(Reporter.class, Arguments.class);
                profilers.add(constructor.newInstance(reporter, arguments));
            } catch (NoSuchMethodException e) {
                handleInitializationException(profiler, e);
            } catch (InvocationTargetException e) {
                handleInitializationException(profiler, e);
            } catch (InstantiationException e) {
                handleInitializationException(profiler, e);
            } catch (IllegalAccessException e) {
                handleInitializationException(profiler, e);
            }
        }

        scheduleProfilers(profilers);
        registerShutdownHook(profilers);
    }

    /**
     * Schedule profilers with a SchedulerExecutorService
     *
     * @param profilers Collection of profilers to schedule
     */
    private static void scheduleProfilers(Collection<Profiler> profilers) {
        // We need to convert to an ExitingScheduledExecutorService so the JVM shuts down
        // when the main thread finishes
        ScheduledExecutorService scheduledExecutorService = MoreExecutors.getExitingScheduledExecutorService(
                (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(profilers.size(), new ProfilerThreadFactory()));

        for (Profiler profiler : profilers) {
            ProfilerWorkerThread worker = new ProfilerWorkerThread(profiler);
            scheduledExecutorService.scheduleAtFixedRate(worker, EXECUTOR_DELAY, profiler.getPeriod(), profiler.getTimeUnit());
        }
    }

    /**
     * Register a shutdown hook to flush profiler data to StatsD
     *
     * @param profilers The profilers to flush at shutdown
     */
    private static void registerShutdownHook(Collection<Profiler> profilers) {
        Thread shutdownHook = new Thread(new ProfilerShutdownHookWorker(profilers));
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    /**
     * Uniformed handling of initialization exception since Java 6 can't do multiple catch
     *
     * @param profiler
     * @param cause
     */
    private static void handleInitializationException(final Class<? extends Profiler> profiler, final Exception cause) {
        throw new RuntimeException("Unable to instantiate " + profiler.getSimpleName(), cause);
    }
}
