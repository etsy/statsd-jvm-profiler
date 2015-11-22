package com.etsy.statsd.profiler;

import com.etsy.statsd.profiler.reporter.Reporter;
import com.etsy.statsd.profiler.server.ProfilerServer;
import com.etsy.statsd.profiler.worker.ProfilerShutdownHookWorker;
import com.etsy.statsd.profiler.worker.ProfilerThreadFactory;
import com.etsy.statsd.profiler.worker.ProfilerWorkerThread;
import com.google.common.util.concurrent.MoreExecutors;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

/**
 * javaagent profiler using StatsD as a backend
 *
 * @author Andrew Johnson
 */
public class Agent {
    public static final int EXECUTOR_DELAY = 0;

    static AtomicReference<Boolean> isRunning = new AtomicReference<>(true);
    static LinkedList<String> errors = new LinkedList<>();
    /**
     * Start the profiler
     *
     * @param args Profiler arguments
     * @param instrumentation Instrumentation agent
     */
    public static void premain(final String args, final Instrumentation instrumentation) {
        Arguments arguments = Arguments.parseArgs(args);

        Reporter reporter = instantiate(arguments.reporter, Reporter.CONSTRUCTOR_PARAM_TYPES, arguments);

        Collection<Profiler> profilers = new ArrayList<>();
        for (Class<? extends Profiler> profiler : arguments.profilers) {
            profilers.add(instantiate(profiler, Profiler.CONSTRUCTOR_PARAM_TYPES, reporter, arguments));
        }

        scheduleProfilers(profilers, arguments.httpPort);
        registerShutdownHook(profilers);
    }

    /**
     * Schedule profilers with a SchedulerExecutorService
     *
     * @param profilers Collection of profilers to schedule
     */
    private static void scheduleProfilers(Collection<Profiler> profilers, int httpPort) {
        // We need to convert to an ExitingScheduledExecutorService so the JVM shuts down
        // when the main thread finishes
        ScheduledExecutorService scheduledExecutorService = MoreExecutors.getExitingScheduledExecutorService(
                (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(profilers.size(), new ProfilerThreadFactory()));

        Map<String, ScheduledFuture<?>> runningProfilers = new HashMap<>(profilers.size());
        Map<String, Profiler> activeProfilers = new HashMap<>(profilers.size());
        for (Profiler profiler : profilers) {
            activeProfilers.put(profiler.getClass().getSimpleName(), profiler);
            ProfilerWorkerThread worker = new ProfilerWorkerThread(profiler, errors);
            ScheduledFuture future =  scheduledExecutorService.scheduleAtFixedRate(worker, EXECUTOR_DELAY, profiler.getPeriod(), profiler.getTimeUnit());
            runningProfilers.put(profiler.getClass().getSimpleName(), future);
        }
        ProfilerServer.startServer(runningProfilers, activeProfilers, httpPort, isRunning, errors);
    }

    /**
     * Register a shutdown hook to flush profiler data to StatsD
     *
     * @param profilers The profilers to flush at shutdown
     */
    private static void registerShutdownHook(Collection<Profiler> profilers) {
        Thread shutdownHook = new Thread(new ProfilerShutdownHookWorker(profilers, isRunning));
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    /**
     * Uniformed handling of initialization exception
     *
     * @param clazz The class that could not be instantiated
     * @param cause The underlying exception
     */
    private static void handleInitializationException(final Class<?> clazz, final Exception cause) {
        throw new RuntimeException("Unable to instantiate " + clazz.getSimpleName(), cause);
    }

    /**
     * Instantiate an object
     *
     * @param clazz A Class representing the type of object to instantiate
     * @param parameterTypes The parameter types for the constructor
     * @param initArgs The values to pass to the constructor
     * @param <T> The type of the object to instantiate
     * @return A new instance of type T
     */
    private static <T> T instantiate(final Class<T> clazz, Class<?>[] parameterTypes, Object... initArgs) {
        try {
            Constructor<T> constructor = clazz.getConstructor(parameterTypes);
            return constructor.newInstance(initArgs);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            handleInitializationException(clazz, e);
        }

        return null;
    }
}
