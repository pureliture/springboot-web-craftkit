package com.springboot.craftkit.framework.rest.client;

import com.springboot.craftkit.framework.rest.setting.CorrelationProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.UUID;

/**
 * Ensures a correlation/transaction header exists on outbound requests. If inbound request has one,
 * it propagates; otherwise, it generates a new UUID value.
 */
public class CorrelationIdClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final CorrelationProperties properties;

    public CorrelationIdClientHttpRequestInterceptor(CorrelationProperties properties) {
        this.properties = properties;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (properties.isEnabled()) {
            String headerName = properties.getHeaderName();
            String value = findInboundHeader(headerName);
            if (value == null || value.isEmpty()) {
                value = UUID.randomUUID().toString();
            }
            if (!request.getHeaders().containsKey(headerName)) {
                request.getHeaders().add(headerName, value);
            }
        }
        return execution.execute(request, body);
    }

    private String findInboundHeader(String headerName) {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            HttpServletRequest req = sra.getRequest();
            return req.getHeader(headerName);
        }
        return null;
    }
}
