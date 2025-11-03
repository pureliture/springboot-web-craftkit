package com.teststrategy.multimodule.maven.sf.framework.rest;

import com.teststrategy.multimodule.maven.sf.framework.rest.client.CircuitBreakerInstanceNamer;
import com.teststrategy.multimodule.maven.sf.framework.rest.client.CircuitBreakerInterceptor;
import com.teststrategy.multimodule.maven.sf.framework.rest.setting.CircuitBreakerProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CircuitBreakerInterceptorTest {

    @Test
    void opensCircuit_afterFailures_andShortCircuitsSubsequentCalls() throws IOException {
        CircuitBreakerConfig cfg = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(2)
                .minimumNumberOfCalls(2)
                .failureRateThreshold(50.0f)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .build();
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(cfg);

        CircuitBreakerProperties props = new CircuitBreakerProperties();
        CircuitBreakerInstanceNamer namer = new CircuitBreakerInstanceNamer(props);
        CircuitBreakerInterceptor interceptor = new CircuitBreakerInterceptor(registry, props, namer);

        HttpRequest req = new StubRequest(URI.create("http://example.org/test"), HttpMethod.GET);
        ClientHttpRequestExecution exec = (request, body) -> { throw new IOException("fail"); };

        // First two calls fail and should be recorded
        assertThatThrownBy(() -> interceptor.intercept(req, new byte[0], exec)).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> interceptor.intercept(req, new byte[0], exec)).isInstanceOf(IOException.class);

        // Third call should be short-circuited by open circuit
        assertThatThrownBy(() -> interceptor.intercept(req, new byte[0], exec))
                .hasMessageContaining("CircuitBreaker 'GET example.org' is OPEN");
    }

    @Test
    void passesThrough_onSuccess() throws IOException {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();
        CircuitBreakerProperties props = new CircuitBreakerProperties();
        CircuitBreakerInstanceNamer namer = new CircuitBreakerInstanceNamer(props);
        CircuitBreakerInterceptor interceptor = new CircuitBreakerInterceptor(registry, props, namer);

        HttpRequest req = new StubRequest(URI.create("http://example.org/ping"), HttpMethod.GET);
        ClientHttpRequestExecution exec = (request, body) -> new FixedResponse(200);

        ClientHttpResponse resp = interceptor.intercept(req, new byte[0], exec);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
    }

    // helpers
    static class StubRequest implements HttpRequest {
        private final URI uri; private final HttpMethod method; private final HttpHeaders headers = new HttpHeaders();
        StubRequest(URI uri, HttpMethod method) { this.uri = uri; this.method = method; }
        @Override public HttpMethod getMethod() { return method; }
        public String getMethodValue() { return method.name(); }
        @Override public URI getURI() { return uri; }
        @Override public HttpHeaders getHeaders() { return headers; }
    }

    static class FixedResponse implements ClientHttpResponse {
        private final int status;
        FixedResponse(int status) { this.status = status; }
        @Override public org.springframework.http.HttpStatusCode getStatusCode() { return org.springframework.http.HttpStatusCode.valueOf(status); }
        @Override public String getStatusText() { return String.valueOf(status); }
        @Override public void close() { }
        @Override public InputStream getBody() { return new ByteArrayInputStream(new byte[0]); }
        @Override public HttpHeaders getHeaders() { return new HttpHeaders(); }
    }
}
