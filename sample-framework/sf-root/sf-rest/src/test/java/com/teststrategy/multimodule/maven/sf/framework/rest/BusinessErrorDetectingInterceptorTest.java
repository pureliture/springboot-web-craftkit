package com.teststrategy.multimodule.maven.sf.framework.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teststrategy.multimodule.maven.sf.framework.rest.client.BusinessErrorDetectingInterceptor;
import com.teststrategy.multimodule.maven.sf.framework.rest.client.BusinessErrorException;
import com.teststrategy.multimodule.maven.sf.framework.rest.setting.ErrorHandlerProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BusinessErrorDetectingInterceptorTest {

    @Test
    void throwsBusinessError_on2xxJsonWithErrorCode() throws IOException {
        ErrorHandlerProperties props = new ErrorHandlerProperties();
        props.setEnabled(true);
        props.setSuccessCodesCsv("OK,SUCCESS");
        BusinessErrorDetectingInterceptor interceptor = new BusinessErrorDetectingInterceptor(props, new ObjectMapper());

        byte[] body = "{\n  \"code\": \"ERROR\", \n  \"message\": \"Failed\"\n}".getBytes();
        StubHttpRequest request = new StubHttpRequest(URI.create("https://example.org"), HttpMethod.GET);
        ClientHttpRequestExecution exec = (req, reqBody) -> new FixedResponse(200, MediaType.APPLICATION_JSON, body);

        assertThatThrownBy(() -> interceptor.intercept(request, new byte[0], exec))
                .isInstanceOf(BusinessErrorException.class)
                .hasMessageContaining("Business error detected")
                .satisfies(ex -> {
                    BusinessErrorException be = (BusinessErrorException) ex;
                    assertThat(be.getBusinessCode()).isEqualTo("ERROR");
                    assertThat(be.getBusinessMessage()).isEqualTo("Failed");
                    assertThat(be.getStatusCode().value()).isEqualTo(200);
                });
    }

    @Test
    void passesThrough_on2xxJsonWithSuccessCode_andBodyReusable() throws IOException {
        ErrorHandlerProperties props = new ErrorHandlerProperties();
        props.setEnabled(true);
        props.setSuccessCodesCsv("OK,SUCCESS");
        BusinessErrorDetectingInterceptor interceptor = new BusinessErrorDetectingInterceptor(props, new ObjectMapper());

        String json = "{\"code\":\"OK\",\"message\":\"fine\",\"data\":{\"x\":1}}";
        byte[] body = json.getBytes();
        StubHttpRequest request = new StubHttpRequest(URI.create("https://example.org"), HttpMethod.GET);
        ClientHttpRequestExecution exec = (req, reqBody) -> new FixedResponse(200, MediaType.APPLICATION_JSON, body);

        ClientHttpResponse resp = interceptor.intercept(request, new byte[0], exec);
        // read twice
        String first = new String(resp.getBody().readAllBytes());
        String second = new String(resp.getBody().readAllBytes());
        assertThat(first).isEqualTo(json);
        assertThat(second).isEqualTo(json);
    }

    // --- test helpers ---
    static class StubHttpRequest implements org.springframework.http.HttpRequest {
        private final URI uri; private final HttpMethod method; private final HttpHeaders headers = new HttpHeaders();
        StubHttpRequest(URI uri, HttpMethod method) { this.uri = uri; this.method = method; }
        public HttpMethod getMethod() { return method; }
        public String getMethodValue() { return method.name(); }
        public URI getURI() { return uri; }
        public HttpHeaders getHeaders() { return headers; }
    }

    static class FixedResponse implements ClientHttpResponse {
        private final int status; private final MediaType mediaType; private final byte[] body;
        FixedResponse(int status, MediaType mediaType, byte[] body) {
            this.status = status; this.mediaType = mediaType; this.body = body;
        }
        @Override public HttpStatusCode getStatusCode() { return HttpStatusCode.valueOf(status); }
        @Override public String getStatusText() { return String.valueOf(status); }
        @Override public void close() { }
        @Override public InputStream getBody() { return new ByteArrayInputStream(body); }
        @Override public HttpHeaders getHeaders() {
            HttpHeaders h = new HttpHeaders();
            h.setContentType(mediaType);
            return h;
        }
    }
}
