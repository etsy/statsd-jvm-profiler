package com.etsy.statsd.profiler.server;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.RouteMatcher;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * Sets up a simple embedded HTTP server for interacting with the profiler while it runs
 *
 * @author Andrew Johnson
 */
public class ProfilerServer {
    private static final Vertx vertx = VertxFactory.newVertx();

    /**
     * Create an embedded HTTP server
     *
     * @param activeProfilers The active profilers
     * @param port The port on which to bind the server
     */
    public static void createServer(final Map<String, ScheduledFuture<?>> activeProfilers, final int port) {
        HttpServer server = vertx.createHttpServer();
        RouteMatcher matcher = new RouteMatcher();
        matcher.get("/profilers", RequestHandler.handleGetProfilers(activeProfilers));
        matcher.get("/disable/:profiler", RequestHandler.handleDisableProfiler(activeProfilers));

        server.requestHandler(matcher);
        server.listen(port);
    }
}
