package com.etsy.statsd.profiler.server;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

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

    public static Collection<String> getEnabledProfilers(final Map<String, ScheduledFuture<?>> activeProfilers) {
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
