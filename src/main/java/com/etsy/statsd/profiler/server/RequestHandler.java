package com.etsy.statsd.profiler.server;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * Handler for HTTP requests to the profiler server
 *
 * @author Andrew Johnson
 */
public class RequestHandler {

    /**
     * Construct a RouteMatcher for the supported routes
     *
     * @param activeProfilers The active profilers
     * @return A RouteMatcher that matches all supported routes
     */
    public static RouteMatcher getMatcher(final Map<String, ScheduledFuture<?>> activeProfilers) {
        RouteMatcher matcher = new RouteMatcher();
        matcher.get("/profilers", RequestHandler.handleGetProfilers(activeProfilers));
        matcher.get("/disable/:profiler", RequestHandler.handleDisableProfiler(activeProfilers));

        return matcher;
    }

    /**
     * Handle a GET to /profilers
     *
     * @param activeProfilers The active profilers
     * @return A Handler that handles a request to the /profilers endpoint
     */
    public static Handler<HttpServerRequest> handleGetProfilers(final Map<String, ScheduledFuture<?>> activeProfilers) {
        return new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest httpServerRequest) {
                httpServerRequest.response().end(Joiner.on("\n").join(getEnabledProfilers(activeProfilers)));
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
        }), new Function<Map.Entry<String,ScheduledFuture<?>>, String>() {
            @Override
            public String apply(Map.Entry<String, ScheduledFuture<?>> input) {
                return input.getKey();
            }
        });
    }
}
