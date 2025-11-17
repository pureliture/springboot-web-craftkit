package com.springboot.craftkit.framework.rest.client;

import com.springboot.craftkit.framework.rest.setting.HttpClientRetryProperties;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;

import java.io.IOException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * HttpClient5 retry strategy that supports both IOException retries and HTTP status-based retries
 * with configurable backoff and method filtering.
 */
public class HttpClient5RetryStrategy implements HttpRequestRetryStrategy {

    private final int maxAttempts;
    private final Duration baseInterval;
    private final HttpClientRetryProperties.BackoffStrategy backoffStrategy;
    private final double backoffMultiplier;
    private final Duration maxInterval;
    private final boolean retryOnIoExceptions;
    private final boolean respectRetryAfter;
    private final boolean retrySentNonIdempotent;
    private final Set<String> retryMethods; // UPPERCASE
    private final Set<Integer> retryStatuses; // if empty -> all 5xx

    public HttpClient5RetryStrategy(HttpClientRetryProperties props) {
        this.maxAttempts = Math.max(1, props.getMaxAttempts());
        this.baseInterval = props.getInterval() == null ? Duration.ofMillis(200) : props.getInterval();
        this.backoffStrategy = props.getBackoffStrategy() == null ? HttpClientRetryProperties.BackoffStrategy.EXPONENTIAL : props.getBackoffStrategy();
        this.backoffMultiplier = props.getBackoffMultiplier() <= 0 ? 2.0d : props.getBackoffMultiplier();
        this.maxInterval = props.getMaxInterval();
        this.retryOnIoExceptions = props.isRetryOnIoExceptions();
        this.respectRetryAfter = props.isRespectRetryAfter();
        this.retrySentNonIdempotent = props.isRetrySentNonIdempotent();
        this.retryMethods = new HashSet<>();
        if (props.getMethods() == null || props.getMethods().isEmpty()) {
            // defaults
            retryMethods.add("GET");
            retryMethods.add("HEAD");
            retryMethods.add("OPTIONS");
        } else {
            props.getMethods().forEach(m -> { if (m != null) retryMethods.add(m.toUpperCase(Locale.ROOT)); });
        }
        this.retryStatuses = new HashSet<>();
        if (props.getRetryOnStatuses() != null) this.retryStatuses.addAll(props.getRetryOnStatuses());
    }

    @Override
    public boolean retryRequest(HttpRequest request, IOException exception, int execCount, HttpContext context) {
        if (!retryOnIoExceptions || exception == null) return false;
        if (!isUnderAttemptLimit(execCount)) return false;
        if (!isMethodRetryable(request)) return false;
        // Non-idempotent methods are retried only if explicitly allowed
        if (!retrySentNonIdempotent && !isMethodIdempotent(request)) return false;
        return true;
    }

    @Override
    public boolean retryRequest(HttpResponse response, int execCount, HttpContext context) {
        if (response == null) return false;
        if (!isUnderAttemptLimit(execCount)) return false;
        int code = response.getCode();
        boolean statusRetry;
        if (retryStatuses.isEmpty()) {
            statusRetry = code >= 500 && code < 600; // default: all 5xx
        } else {
            statusRetry = retryStatuses.contains(code);
        }
        if (!statusRetry) return false;
        HttpRequest request = (HttpRequest) context.getAttribute(HttpClientContext.HTTP_REQUEST);
        return isMethodRetryable(request);
    }

    @Override
    public TimeValue getRetryInterval(HttpResponse response, int execCount, HttpContext context) {
        if (respectRetryAfter && response != null) {
            Header h = response.getFirstHeader("Retry-After");
            if (h != null) {
                Long ra = parseRetryAfterSeconds(h.getValue());
                if (ra != null) {
                    return TimeValue.ofSeconds(ra);
                }
            }
        }
        // execCount is 1 for the first attempt; interval should be for the next retry
        int attemptIndex = Math.max(0, execCount - 1);
        long millis;
        if (backoffStrategy == HttpClientRetryProperties.BackoffStrategy.FIXED) {
            millis = baseInterval.toMillis();
        } else {
            double factor = Math.pow(backoffMultiplier, attemptIndex);
            millis = (long) Math.max(0, Math.round(baseInterval.toMillis() * factor));
        }
        if (maxInterval != null && millis > maxInterval.toMillis()) {
            millis = maxInterval.toMillis();
        }
        return TimeValue.ofMilliseconds(millis);
    }

    private boolean isUnderAttemptLimit(int execCount) {
        // execCount counts the number of times this request has been executed
        return execCount < maxAttempts; // allow retry when we still have remaining attempts
    }

    private boolean isMethodRetryable(HttpRequest request) {
        if (request == null) return false;
        String method = request.getMethod();
        return method != null && retryMethods.contains(method.toUpperCase(Locale.ROOT));
    }

    private boolean isMethodIdempotent(HttpRequest request) {
        if (request == null || request.getMethod() == null) return false;
        String m = request.getMethod().toUpperCase(Locale.ROOT);
        return "GET".equals(m) || "HEAD".equals(m) || "OPTIONS".equals(m) || "DELETE".equals(m) || "TRACE".equals(m);
    }

    private static Long parseRetryAfterSeconds(String value) {
        try {
            // If it's a delta-seconds
            return Long.parseLong(value.trim());
        } catch (Exception ignored) {}
        try {
            // HTTP-date, e.g., Sun, 06 Nov 1994 08:49:37 GMT
            DateTimeFormatter rfc1123 = DateTimeFormatter.RFC_1123_DATE_TIME;
            long epochSec = java.time.ZonedDateTime.parse(value, rfc1123).toEpochSecond();
            long now = java.time.Instant.now().getEpochSecond();
            long delta = Math.max(0, epochSec - now);
            return delta;
        } catch (Exception ignored) {}
        return null;
    }
}
