package com.springboot.craftkit.framework.rest.client;

import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.beans.factory.DisposableBean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background task to periodically evict expired/idle connections from Apache HttpClient5.
 */
public class IdleConnectionEvictor implements DisposableBean {
    private final PoolingHttpClientConnectionManager connectionManager;
    private final ScheduledExecutorService scheduler;
    private final long idleMs;
    private final long periodMs;

    public IdleConnectionEvictor(PoolingHttpClientConnectionManager connectionManager, long idleMs, long periodMs) {
        this.connectionManager = connectionManager;
        this.idleMs = idleMs;
        this.periodMs = periodMs;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "sf-rest-idle-evictor");
            t.setDaemon(true);
            return t;
        });
        this.scheduler.scheduleAtFixedRate(this::evict, periodMs, periodMs, TimeUnit.MILLISECONDS);
    }

    private void evict() {
        try {
            connectionManager.closeExpired();
            connectionManager.closeIdle(TimeValue.ofMilliseconds(idleMs));
        } catch (Exception ignore) {
        }
    }

    @Override
    public void destroy() {
        scheduler.shutdownNow();
    }
}
