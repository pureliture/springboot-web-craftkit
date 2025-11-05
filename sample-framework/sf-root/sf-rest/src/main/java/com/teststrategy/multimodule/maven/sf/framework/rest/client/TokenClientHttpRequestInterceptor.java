package com.teststrategy.multimodule.maven.sf.framework.rest.client;

import com.teststrategy.multimodule.maven.sf.framework.rest.setting.OAuthClientProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Simple interceptor that adds a static Bearer token when enabled.
 * This is a neutral placeholder to demonstrate token injection without external systems.
 */
public class TokenClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final OAuthClientProperties properties;

    public TokenClientHttpRequestInterceptor(OAuthClientProperties properties) {
        this.properties = properties;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (properties.isEnabled()) {
            String token = properties.getStaticToken();
            if (token != null && !token.isEmpty()) {
                request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            }
        }
        return execution.execute(request, body);
    }
}
