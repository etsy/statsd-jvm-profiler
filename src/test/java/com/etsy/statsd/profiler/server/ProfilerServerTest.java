package com.etsy.statsd.profiler.server;

import com.etsy.statsd.profiler.Profiler;
import com.etsy.statsd.profiler.profilers.MockProfiler1;
import com.etsy.statsd.profiler.profilers.MockProfiler2;
import com.etsy.statsd.profiler.worker.ProfilerThreadFactory;
import com.etsy.statsd.profiler.worker.ProfilerWorkerThread;
import com.google.common.base.Joiner;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProfilerServerTest {
    private Map<String, Profiler> activeProfilers;
    private int port;
    private AtomicReference<Boolean> isRunning;
    private List<String> errors;
    private CloseableHttpClient client;

    @Before
    public void setup() throws IOException {
        MockProfiler1 profiler1 = new MockProfiler1(new HashSet<String>());
        MockProfiler2 profiler2 = new MockProfiler2(new HashSet<String>());

        activeProfilers = new HashMap<>();
        activeProfilers.put("MockProfiler1", profiler1);
        activeProfilers.put("MockProfiler2", profiler2);

        ServerSocket s = new ServerSocket(0); // This gives us a random open port
        port = s.getLocalPort();
        s.close();

        isRunning = new AtomicReference<>(true);
        errors = new ArrayList<>();
        errors.add("example error");

        Map<String, ScheduledFuture<?>> runningProfilers = new HashMap<>();
        ProfilerWorkerThread worker1 = new ProfilerWorkerThread(profiler1, errors);
        ProfilerWorkerThread worker2 = new ProfilerWorkerThread(profiler2, errors);
        ScheduledExecutorService scheduledExecutorService = MoreExecutors.getExitingScheduledExecutorService(
                (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(2, new ProfilerThreadFactory()));
        ScheduledFuture future1 = scheduledExecutorService.scheduleAtFixedRate(worker1, 0, profiler1.getPeriod(), profiler1.getTimeUnit());
        ScheduledFuture future2 = scheduledExecutorService.scheduleAtFixedRate(worker2, 0, profiler2.getPeriod(), profiler2.getTimeUnit());
        runningProfilers.put("MockProfiler1", future1);
        runningProfilers.put("MockProfiler2", future2);

        ProfilerServer.startServer(runningProfilers, activeProfilers, port, isRunning, errors);
        client = HttpClients.createDefault();
    }

    @Test
    public void testDisableProfiler() throws IOException {
        String profilerString = "MockProfiler1\nMockProfiler2";
        httpRequestTest("profilers", profilerString);

        String profilerToDisable = "MockProfiler1";
        httpRequestTest(String.format("disable/%s", profilerToDisable), String.format("Disabled profiler %s", profilerToDisable));

        profilerString = "MockProfiler2";
        httpRequestTest("profilers", profilerString);
    }

    @Test
    public void testProfilerStatus() throws IOException {
        String profilerName = "MockProfiler1";
        long recordedStats = activeProfilers.get(profilerName).getRecordedStats();
        httpRequestTest(String.format("status/profiler/%s", profilerName), String.format("Recorded stats %d\n", recordedStats));
    }

    @Test
    public void testProfilers() throws IOException {
        String profilerString = "MockProfiler1\nMockProfiler2";
        httpRequestTest("profilers", profilerString);
    }

    @Test
    public void testErrors() throws IOException {
        String errorString = Joiner.on("\n").join(errors);
        httpRequestTest("errors", String.format("Errors: %s", errorString));
    }

    @Test
    public void testIsRunning() throws IOException {
        httpRequestTest("isRunning", String.format("isRunning: %s", isRunning.get().toString()));
    }

    private void httpRequestTest(String path, String expectedBody) throws IOException {
        HttpRequestBase get = new HttpGet(String.format("http://localhost:%d/%s", port, path));
        CloseableHttpResponse response = client.execute(get);

        int statusCode = response.getStatusLine().getStatusCode();
        assertEquals(200, statusCode);

        HttpEntity entity = response.getEntity();
        assertNotNull(entity);
        String responseBody = EntityUtils.toString(entity);
        assertEquals(expectedBody, responseBody);

        response.close();
    }
}
