package com.springboot.craftkit.framework.rest.client;

import com.springboot.craftkit.framework.rest.setting.ForwardHeadersProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.List;

/**
 * Copies selected inbound HTTP headers to outbound requests when a servlet request context is present.
 */
public class ForwardHeadersClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final ForwardHeadersProperties properties;

    public ForwardHeadersClientHttpRequestInterceptor(ForwardHeadersProperties properties) {
        this.properties = properties;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {

        if (!properties.isEnabled()) {
            return execution.execute(request, body);
        }

        HttpServletRequest inboundRequest = this.currentRequest();
        if (inboundRequest == null) {
            return execution.execute(request, body);
        }

        HttpHeaders outboundHeaders = request.getHeaders();
        List<String> headerNames = properties.getNames();
        this.copyHeadersIfAbsent(inboundRequest, outboundHeaders, headerNames);

        return execution.execute(request, body);
    }

    @Nullable
    private HttpServletRequest currentRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            return sra.getRequest();
        }
        return null;
    }

    private void copyHeadersIfAbsent(HttpServletRequest inboundRequest, HttpHeaders outboundHeaders, @Nullable List<String> headerNames) {
        if (headerNames == null || headerNames.isEmpty()) {
            return;
        }
        for (String headerName : headerNames) {
            if (!org.springframework.util.StringUtils.hasText(headerName)) {
                continue;
            }
            String headerValue = inboundRequest.getHeader(headerName);
            if (org.springframework.util.StringUtils.hasText(headerValue) && !outboundHeaders.containsKey(headerName)) {
                outboundHeaders.add(headerName, headerValue);
            }
        }
    }
}
