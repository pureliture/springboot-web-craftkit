package com.teststrategy.multimodule.maven.sf.framework.rest.setting;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = HttpClientEvictorProperties.PREFIX)
public class HttpClientEvictorProperties {
    public static final String PREFIX = "sf-rest.http-client.evictor";

    /** Enable background idle connection eviction thread */
    private boolean enabled = false;

    /** Idle time in milliseconds before a connection is eligible for eviction */
    private long idleTimeMillis = 30_000L;

    /** Check interval in milliseconds for the eviction thread */
    private long checkIntervalMillis = 5_000L;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public long getIdleTimeMillis() { return idleTimeMillis; }
    public void setIdleTimeMillis(long idleTimeMillis) { this.idleTimeMillis = idleTimeMillis; }

    public long getCheckIntervalMillis() { return checkIntervalMillis; }
    public void setCheckIntervalMillis(long checkIntervalMillis) { this.checkIntervalMillis = checkIntervalMillis; }
}
