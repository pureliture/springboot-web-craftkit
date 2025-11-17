package com.springboot.craftkit.framework.rest.setting;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = HttpClientProperties.PREFIX)
public class HttpClientProperties {
    public static final String PREFIX = "sf-rest.http-client";

    /**
     * Connection timeout for outbound REST calls.
     */
    private Duration connectTimeout = Duration.ofSeconds(5);

    /**
     * Read timeout for outbound REST calls.
     */
    private Duration readTimeout = Duration.ofSeconds(30);

    /**
     * Maximum total connections (applies when using a pooling client such as Apache HttpClient).
     */
    private int maxConnTotal = 200;

    /**
     * Maximum connections per route (applies when using a pooling client such as Apache HttpClient).
     */
    private int maxConnPerRoute = 50;

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getMaxConnTotal() {
        return maxConnTotal;
    }

    public void setMaxConnTotal(int maxConnTotal) {
        this.maxConnTotal = maxConnTotal;
    }

    public int getMaxConnPerRoute() {
        return maxConnPerRoute;
    }

    public void setMaxConnPerRoute(int maxConnPerRoute) {
        this.maxConnPerRoute = maxConnPerRoute;
    }
}
