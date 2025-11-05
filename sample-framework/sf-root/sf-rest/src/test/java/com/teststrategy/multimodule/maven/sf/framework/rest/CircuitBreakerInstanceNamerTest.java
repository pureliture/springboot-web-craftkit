package com.teststrategy.multimodule.maven.sf.framework.rest;

import com.teststrategy.multimodule.maven.sf.framework.rest.client.CircuitBreakerInstanceNamer;
import com.teststrategy.multimodule.maven.sf.framework.rest.client.DomainApiContext;
import com.teststrategy.multimodule.maven.sf.framework.rest.setting.SfRestCircuitBreakerProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class CircuitBreakerInstanceNamerTest {

    @AfterEach
    void cleanup() { DomainApiContext.clear(); }

    @Test
    void usesDomainApi_whenAvailable() {
        SfRestCircuitBreakerProperties props = new SfRestCircuitBreakerProperties();
        props.setInstanceFrom(SfRestCircuitBreakerProperties.InstanceFrom.DOMAIN_API);
        CircuitBreakerInstanceNamer namer = new CircuitBreakerInstanceNamer(props);

        DomainApiContext.setCurrentDomainApi("apim.resource");
        HttpRequest req = new StubRequest(URI.create("http://example.org/test"), HttpMethod.GET);
        assertThat(namer.name(req)).isEqualTo("apim.resource");
    }

    @Test
    void fallsBackToMethodAndHost_whenNoDomainApi() {
        SfRestCircuitBreakerProperties props = new SfRestCircuitBreakerProperties();
        props.setInstanceFrom(SfRestCircuitBreakerProperties.InstanceFrom.DOMAIN_API);
        CircuitBreakerInstanceNamer namer = new CircuitBreakerInstanceNamer(props);

        HttpRequest req = new StubRequest(URI.create("http://EXAMPLE.org:8080/test"), HttpMethod.POST);
        assertThat(namer.name(req)).isEqualTo("POST example.org:8080");
    }

    static class StubRequest implements HttpRequest {
        private final URI uri; private final HttpMethod method; private final HttpHeaders headers = new HttpHeaders();
        StubRequest(URI uri, HttpMethod method) { this.uri = uri; this.method = method; }
        @Override public HttpMethod getMethod() { return method; }
        public String getMethodValue() { return method.name(); }
        @Override public URI getURI() { return uri; }
        @Override public HttpHeaders getHeaders() { return headers; }
    }
}
