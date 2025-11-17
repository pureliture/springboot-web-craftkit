package com.springboot.craftkit.framework.rest.client;

import com.springboot.craftkit.framework.rest.setting.SfRestCircuitBreakerProperties;
import org.springframework.http.HttpRequest;

import java.net.URI;
import java.util.Locale;

/**
 * Derives CircuitBreaker instance id based on configuration:
 * - DOMAIN_API: use current '{@domain.api}' from DomainApiContext if available
 * - URI: use "METHOD host[:port]" derived from request URI
 */
public class CircuitBreakerInstanceNamer {

    private final SfRestCircuitBreakerProperties properties;

    public CircuitBreakerInstanceNamer(SfRestCircuitBreakerProperties properties) {
        this.properties = properties;
    }

    public String name(HttpRequest request) {
        if (properties.getInstanceFrom() == SfRestCircuitBreakerProperties.InstanceFrom.DOMAIN_API) {
            String id = DomainApiContext.getCurrentDomainApi();
            if (id != null && !id.isBlank()) {
                return id;
            }
        }
        return fallbackFromUri(request);
    }

    private static String fallbackFromUri(HttpRequest request) {
        URI uri = request.getURI();
        String host = uri.getHost();
        int port = uri.getPort();
        StringBuilder sb = new StringBuilder();
        String method = request.getMethod() != null ? request.getMethod().name() : "GET";
        sb.append(method);
        sb.append(' ');
        if (host != null) {
            sb.append(host.toLowerCase(Locale.ROOT));
        } else {
            sb.append("unknown");
        }
        if (port > 0) {
            sb.append(':').append(port);
        }
        return sb.toString();
    }
}
