package com.teststrategy.multimodule.maven.sf.framework.rest.client;

import com.teststrategy.multimodule.maven.sf.framework.rest.setting.ForwardHeadersProperties;
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
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (properties.isEnabled()) {
            HttpServletRequest inbound = currentRequest();
            if (inbound != null) {
                HttpHeaders headers = request.getHeaders();
                List<String> names = properties.getNames();
                if (names != null) {
                    for (String name : names) {
                        if (name == null || name.isEmpty()) continue;
                        String value = inbound.getHeader(name);
                        if (value != null && !value.isEmpty() && !headers.containsKey(name)) {
                            headers.add(name, value);
                        }
                    }
                }
            }
        }
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
}
