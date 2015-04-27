package com.etsy.statsd.profiler.server;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.http.HttpServer;

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
     * Start an embedded HTTP server
     *
     * @param activeProfilers The active profilers
     * @param port The port on which to bind the server
     */
    public static void startServer(final Map<String, ScheduledFuture<?>> activeProfilers, final int port) {
        HttpServer server = vertx.createHttpServer();
        server.requestHandler(RequestHandler.getMatcher(activeProfilers));
        server.listen(port);
    }
}