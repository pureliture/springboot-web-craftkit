package com.teststrategy.multimodule.maven.sf.framework.rest;

import com.teststrategy.multimodule.maven.sf.framework.application.constant.HeaderConstant;
import com.teststrategy.multimodule.maven.sf.framework.rest.client.CorrelationIdClientHttpRequestInterceptor;
import com.teststrategy.multimodule.maven.sf.framework.rest.client.ForwardHeadersClientHttpRequestInterceptor;
import com.teststrategy.multimodule.maven.sf.framework.rest.setting.CorrelationProperties;
import com.teststrategy.multimodule.maven.sf.framework.rest.setting.ForwardHeadersProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class RestInterceptorsTest {

    @AfterEach
    void cleanup() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void forwardHeadersInterceptor_copiesConfiguredHeaders() throws IOException {
        // arrange inbound request with headers
        MockHttpServletRequest inbound = new MockHttpServletRequest();
        inbound.addHeader("Authorization", "Bearer AAA");
        inbound.addHeader("USER-ID", "demo-user");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(inbound));

        ForwardHeadersProperties props = new ForwardHeadersProperties();
        props.setEnabled(true);
        ForwardHeadersClientHttpRequestInterceptor interceptor = new ForwardHeadersClientHttpRequestInterceptor(props);

        // outbound request stub
        StubHttpRequest outbound = new StubHttpRequest(URI.create("https://example.org"), HttpMethod.GET);
        interceptor.intercept(outbound, new byte[0], new NoopExecution());

        assertThat(outbound.getHeaders().getFirst("Authorization")).isEqualTo("Bearer AAA");
        assertThat(outbound.getHeaders().getFirst("USER-ID")).isEqualTo("demo-user");
    }

    @Test
    void correlationInterceptor_generatesIfMissing() throws IOException {
        // no inbound header
        MockHttpServletRequest inbound = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(inbound));

        CorrelationProperties props = new CorrelationProperties();
        props.setEnabled(true);
        CorrelationIdClientHttpRequestInterceptor interceptor = new CorrelationIdClientHttpRequestInterceptor(props);

        StubHttpRequest outbound = new StubHttpRequest(URI.create("https://example.org"), HttpMethod.GET);
        interceptor.intercept(outbound, new byte[0], new NoopExecution());

        String headerName = HeaderConstant.HEADER_GLOBAL_TRANSACTION_ID;
        assertThat(outbound.getHeaders().getFirst(headerName)).isNotBlank();
    }

    static class StubHttpRequest implements HttpRequest {
        private final URI uri;
        private final HttpMethod method;
        private final HttpHeaders headers = new HttpHeaders();

        StubHttpRequest(URI uri, HttpMethod method) {
            this.uri = uri;
            this.method = method;
        }

        public HttpMethod getMethod() {
            return method;
        }

        public String getMethodValue() {
            return method.name();
        }

        @Override
        public URI getURI() {
            return uri;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }
    }

    static class NoopExecution implements ClientHttpRequestExecution {
        @Override
        public MockClientHttpResponse execute(HttpRequest request, byte[] body) throws IOException {
            return new MockClientHttpResponse(new byte[0], 200);
        }
    }
}
