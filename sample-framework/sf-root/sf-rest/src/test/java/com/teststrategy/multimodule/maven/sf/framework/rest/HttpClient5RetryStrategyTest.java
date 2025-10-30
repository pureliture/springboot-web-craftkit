package com.teststrategy.multimodule.maven.sf.framework.rest;

import com.teststrategy.multimodule.maven.sf.framework.rest.client.HttpClient5RetryStrategy;
import com.teststrategy.multimodule.maven.sf.framework.rest.setting.HttpClientRetryProperties;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpClient5RetryStrategyTest {

    @Test
    void retriesOnIOException_forIdempotentMethodsOnly() {
        HttpClientRetryProperties props = new HttpClientRetryProperties();
        props.setMaxAttempts(3);
        // default methods: GET, HEAD, OPTIONS
        HttpClient5RetryStrategy strategy = new HttpClient5RetryStrategy(props);

        HttpRequest get = new BasicHttpRequest("GET", "/ping");
        HttpRequest post = new BasicHttpRequest("POST", "/ping");
        HttpContext ctx = HttpClientContext.create();

        boolean getRetry = strategy.retryRequest(get, new IOException("boom"), 1, ctx);
        boolean postRetry = strategy.retryRequest(post, new IOException("boom"), 1, ctx);

        assertThat(getRetry).isTrue();
        assertThat(postRetry).isFalse();
    }

    @Test
    void retriesOn5xxStatus_butNotOn4xx() {
        HttpClientRetryProperties props = new HttpClientRetryProperties();
        props.setMaxAttempts(3);
        HttpClient5RetryStrategy strategy = new HttpClient5RetryStrategy(props);

        HttpResponse resp503 = new BasicHttpResponse(503, "Service Unavailable");
        HttpResponse resp404 = new BasicHttpResponse(404, "Not Found");
        HttpContext ctx = HttpClientContext.create();
        ctx.setAttribute(HttpClientContext.HTTP_REQUEST, new BasicHttpRequest("GET", "/x"));

        boolean on503 = strategy.retryRequest(resp503, 1, ctx);
        boolean on404 = strategy.retryRequest(resp404, 1, ctx);

        assertThat(on503).isTrue();
        assertThat(on404).isFalse();
    }

    @Test
    void exponentialBackoffProgression_andCap() {
        HttpClientRetryProperties props = new HttpClientRetryProperties();
        props.setMaxAttempts(5);
        props.setInterval(java.time.Duration.ofMillis(100));
        props.setBackoffStrategy(HttpClientRetryProperties.BackoffStrategy.EXPONENTIAL);
        props.setBackoffMultiplier(2.0);
        props.setMaxInterval(java.time.Duration.ofMillis(250)); // cap at 250ms
        HttpClient5RetryStrategy strategy = new HttpClient5RetryStrategy(props);

        HttpResponse resp = new BasicHttpResponse(503, "Service Unavailable");
        HttpContext ctx = HttpClientContext.create();

        TimeValue t1 = strategy.getRetryInterval(resp, 1, ctx); // attempt index 0
        TimeValue t2 = strategy.getRetryInterval(resp, 2, ctx); // index 1
        TimeValue t3 = strategy.getRetryInterval(resp, 3, ctx); // index 2

        assertThat(t1.toMilliseconds()).isEqualTo(100);
        assertThat(t2.toMilliseconds()).isEqualTo(200);
        assertThat(t3.toMilliseconds()).isEqualTo(250); // capped from 400 to 250
    }
}
