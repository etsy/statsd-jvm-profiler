package com.etsy.statsd.profiler.server;

import com.etsy.statsd.profiler.Profiler;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.http.HttpServer;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sets up a simple embedded HTTP server for interacting with the profiler while it runs
 *
 * @author Andrew Johnson
 */
public class ProfilerServer {
    private static final Logger log = Logger.getLogger(ProfilerServer.class.getName());
    private static final Vertx vertx = VertxFactory.newVertx();

    /**
     * Start an embedded HTTP server
     *
     * @param activeProfilers The active profilers
     * @param port The port on which to bind the server
     */
    public static void startServer(final Map<String, ScheduledFuture<?>> runningProfilers, final Map<String, Profiler> activeProfilers, final int port, final AtomicReference<Boolean> isRunning, final LinkedList<String> errors) {
        final HttpServer server = vertx.createHttpServer();
        server.requestHandler(RequestHandler.getMatcher(runningProfilers, activeProfilers, isRunning, errors));
        server.listen(port, new Handler<AsyncResult<HttpServer>>() {
            @Override
            public void handle(AsyncResult<HttpServer> event) {
                if (event.failed()) {
                    server.close();
                    startServer(runningProfilers, activeProfilers, port + 1, isRunning, errors);
                } else if (event.succeeded()) {
                    log.info("Profiler server started on port " + port);
                }
            }
        });
    }
}