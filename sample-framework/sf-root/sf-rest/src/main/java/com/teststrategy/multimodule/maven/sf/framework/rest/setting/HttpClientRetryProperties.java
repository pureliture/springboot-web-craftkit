package com.teststrategy.multimodule.maven.sf.framework.rest.setting;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Properties controlling Apache HttpClient5 based retry behavior for RestTemplate.
 *
 * Prefix: sf-rest.http-client.retry
 */
@ConfigurationProperties(prefix = HttpClientRetryProperties.PREFIX)
public class HttpClientRetryProperties {
    public static final String PREFIX = "sf-rest.http-client.retry";

    /** Enable HttpClient5-based retry. */
    private boolean enabled = false;

    /** Total attempts including the initial one. */
    private int maxAttempts = 3;

    /** Base interval between attempts. */
    private Duration interval = Duration.ofMillis(200);

    /** Backoff strategy: fixed or exponential. */
    private BackoffStrategy backoffStrategy = BackoffStrategy.EXPONENTIAL;

    /** Multiplier for exponential backoff. */
    private double backoffMultiplier = 2.0d;

    /** Optional maximum backoff interval cap. */
    private Duration maxInterval;

    /** HTTP statuses to retry on. Defaults to all 5xx when null/empty. */
    private Set<Integer> retryOnStatuses = new LinkedHashSet<>();

    /** Retry on IOExceptions (network/transient). */
    private boolean retryOnIoExceptions = true;

    /** Methods eligible for retry. Defaults to GET, HEAD, OPTIONS when empty. */
    private Set<String> methods = new LinkedHashSet<>();

    /** Honor Retry-After header if present on responses. */
    private boolean respectRetryAfter = true;

    /** Allow retry for sent non-idempotent requests (generally unsafe) */
    private boolean retrySentNonIdempotent = false;

    public enum BackoffStrategy { FIXED, EXPONENTIAL }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }

    public Duration getInterval() { return interval; }
    public void setInterval(Duration interval) { this.interval = interval; }

    public BackoffStrategy getBackoffStrategy() { return backoffStrategy; }
    public void setBackoffStrategy(BackoffStrategy backoffStrategy) { this.backoffStrategy = backoffStrategy; }

    public double getBackoffMultiplier() { return backoffMultiplier; }
    public void setBackoffMultiplier(double backoffMultiplier) { this.backoffMultiplier = backoffMultiplier; }

    public Duration getMaxInterval() { return maxInterval; }
    public void setMaxInterval(Duration maxInterval) { this.maxInterval = maxInterval; }

    public Set<Integer> getRetryOnStatuses() { return retryOnStatuses; }
    public void setRetryOnStatuses(Set<Integer> retryOnStatuses) { this.retryOnStatuses = retryOnStatuses; }

    public boolean isRetryOnIoExceptions() { return retryOnIoExceptions; }
    public void setRetryOnIoExceptions(boolean retryOnIoExceptions) { this.retryOnIoExceptions = retryOnIoExceptions; }

    public Set<String> getMethods() { return methods; }
    public void setMethods(Set<String> methods) { this.methods = methods; }

    public boolean isRespectRetryAfter() { return respectRetryAfter; }
    public void setRespectRetryAfter(boolean respectRetryAfter) { this.respectRetryAfter = respectRetryAfter; }

    public boolean isRetrySentNonIdempotent() { return retrySentNonIdempotent; }
    public void setRetrySentNonIdempotent(boolean retrySentNonIdempotent) { this.retrySentNonIdempotent = retrySentNonIdempotent; }
}
