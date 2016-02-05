package com.etsy.statsd.profiler.server;

import com.etsy.statsd.profiler.Profiler;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Handler for HTTP requests to the profiler server
 *
 * @author Andrew Johnson
 */
public class RequestHandler {
    private RequestHandler() { }
    
    /**
     * Construct a RouteMatcher for the supported routes
     *
     * @param activeProfilers The active profilers
     * @return A RouteMatcher that matches all supported routes
     */
    public static Handler<HttpServerRequest> getMatcher(final Router router, final Map<String, ScheduledFuture<?>> runningProfilers, Map<String, Profiler> activeProfilers, AtomicReference<Boolean> isRunning, LinkedList<String> errors) {
        router.route(HttpMethod.GET, "/profilers").handler(RequestHandler.handleGetProfilers(runningProfilers));
        router.route(HttpMethod.GET, "/disable/:profiler").handler(RequestHandler.handleDisableProfiler(runningProfilers));
        router.route(HttpMethod.GET, "/status/profiler/:profiler").handler(RequestHandler.handleProfilerStatus(activeProfilers));
        router.route(HttpMethod.GET, "/errors").handler(RequestHandler.handleErrorMessages(errors));
        router.route(HttpMethod.GET, "/isRunning").handler(RequestHandler.isRunning(isRunning));

        return new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest httpServerRequest) {
                router.accept(httpServerRequest);
            }
        };
    }

    /**
     * Handle a GET to /isRunning
     *
     * @return A Handler that returns all running profilers
     */
    public static Handler<RoutingContext> isRunning(final AtomicReference<Boolean> isRunning) {
        return new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext routingContext) {
                routingContext.response().end(String.format("isRunning: %b", isRunning.get()));
            }
        };
    }

    /**
     * Handle a GET to /profilers
     *
     * @return A Handler that handles a request to the /profilers endpoint
     */
    public static Handler<RoutingContext> handleGetProfilers(final Map<String, ScheduledFuture<?>> runningProfilers) {
        return new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext routingContext) {
                routingContext.response().end(Joiner.on("\n").join(getEnabledProfilers(runningProfilers)));
            }
        };
    }

    /**
     * Handle a GET to /errors
     *
     * @return The last 10 error stacktraces
     */
    public static Handler<RoutingContext> handleErrorMessages(final LinkedList<String> errors) {
        return new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext routingContext) {
                routingContext.response().end("Errors: " + Joiner.on("\n").join(errors));
            }
        };
    }

    /**
     * Handle a GET to /disable/:profiler
     *
     * @param activeProfilers The active profilers
     * @return A Handler that handles a request to the /disable/:profiler endpoint
     */
    public static Handler<RoutingContext> handleDisableProfiler(final Map<String, ScheduledFuture<?>> activeProfilers) {
        return new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext routingContext) {
                String profilerToDisable = routingContext.request().params().get("profiler");
                ScheduledFuture<?> future = activeProfilers.get(profilerToDisable);
                future.cancel(false);
                routingContext.response().end(String.format("Disabled profiler %s", profilerToDisable));
            }
        };
    }

    /**
     * Handle a GET to /status/profiler/:profiler
     *
     * @param activeProfilers The active profilers
     * @return A Handler that handles a request to the /disable/:profiler endpoint
     */
    public static Handler<RoutingContext> handleProfilerStatus(final  Map<String, Profiler> activeProfilers) {
        return new Handler<RoutingContext>() {
            @Override
            public void handle(RoutingContext routingContext) {
                String profilerName = routingContext.request().params().get("profiler");
                Profiler profiler = activeProfilers.get(profilerName);
                routingContext.response().end(String.format("Recorded stats %d\n", profiler.getRecordedStats()));
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
