package com.etsy.statsd.profiler.server;

import com.etsy.statsd.profiler.Profiler;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Handler for HTTP requests to the profiler server
 *
 * @author Andrew Johnson
 */
public final class RequestHandler {
    private RequestHandler() { }
    
    /**
     * Construct a RouteMatcher for the supported routes
     *
     * @param activeProfilers The active profilers
     * @return A RouteMatcher that matches all supported routes
     */
    public static RouteMatcher getMatcher(final Map<String, ScheduledFuture<?>> runningProfilers,  Map<String, Profiler> activeProfilers, AtomicReference<Boolean> isRunning, List<String> errors) {
        RouteMatcher matcher = new RouteMatcher();
        matcher.get("/profilers", RequestHandler.handleGetProfilers(runningProfilers));
        matcher.get("/disable/:profiler", RequestHandler.handleDisableProfiler(runningProfilers));
        matcher.get("/status/profiler/:profiler", RequestHandler.handleProfilerStatus(activeProfilers));
        matcher.get("/errors", RequestHandler.handleErrorMessages(errors));
        matcher.get("/isRunning", RequestHandler.isRunning(isRunning));
        return matcher;
    }

    /**
     * Handle a GET to /isRunning
     *
     * @return A Handler that returns all running profilers
     */
    public static Handler<HttpServerRequest> isRunning(final AtomicReference<Boolean> isRunning) {
        return new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest httpServerRequest) {
                httpServerRequest.response().end(String.format("isRunning: %b", isRunning.get()));
            }
        };
    }

    /**
     * Handle a GET to /profilers
     *
     * @return A Handler that handles a request to the /profilers endpoint
     */
    public static Handler<HttpServerRequest> handleGetProfilers(final Map<String, ScheduledFuture<?>> runningProfilers) {
        return new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest httpServerRequest) {
                httpServerRequest.response().end(Joiner.on("\n").join(getEnabledProfilers(runningProfilers)));
            }
        };
    }

    /**
     * Handle a GET to /errors
     *
     * @return The last 10 error stacktraces
     */
    public static Handler<HttpServerRequest> handleErrorMessages(final List<String> errors) {
        return new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest httpServerRequest) {
                httpServerRequest.response().end("Errors: " + Joiner.on("\n").join(errors));
            }
        };
    }

    /**
     * Handle a GET to /disable/:profiler
     *
     * @param activeProfilers The active profilers
     * @return A Handler that handles a request to the /disable/:profiler endpoint
     */
    public static Handler<HttpServerRequest> handleDisableProfiler(final Map<String, ScheduledFuture<?>> activeProfilers) {
        return new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest httpServerRequest) {
                String profilerToDisable = httpServerRequest.params().get("profiler");
                ScheduledFuture<?> future = activeProfilers.get(profilerToDisable);
                future.cancel(false);
                httpServerRequest.response().end(String.format("Disabled profiler %s", profilerToDisable));
            }
        };
    }

    /**
     * Handle a GET to /status/profiler/:profiler
     *
     * @param activeProfilers The active profilers
     * @return A Handler that handles a request to the /disable/:profiler endpoint
     */
    public static Handler<HttpServerRequest> handleProfilerStatus(final  Map<String, Profiler> activeProfilers) {
        return new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest httpServerRequest) {
                String profilerName = httpServerRequest.params().get("profiler");
                Profiler profiler = activeProfilers.get(profilerName);
                httpServerRequest.response().end(String.format("Recorded stats %d\n", profiler.getRecordedStats()));
            }
        };
    }

    /**
     * Get all enabled profilers
     * @param activeProfilers The active profilers
     * @return A Collection<String> containing the names of profilers that are currently running
     */
    private static Collection<String> getEnabledProfilers(final Map<String, ScheduledFuture<?>> activeProfilers) {
        return Collections2.transform(Collections2.filter(activeProfilers.entrySet(), new Predicate<Map.Entry<String, ScheduledFuture<?>>>() {
            @Override
            public boolean apply(Map.Entry<String, ScheduledFuture<?>> input) {
                return !input.getValue().isDone();
            }
        }), new Function<Map.Entry<String, ScheduledFuture<?>>, String>() {
            @Override
            public String apply(Map.Entry<String, ScheduledFuture<?>> input) {
                return input.getKey();
            }
        });
    }
}
