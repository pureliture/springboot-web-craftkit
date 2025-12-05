package com.springboot.craftkit.framework.filter;

import com.springboot.craftkit.framework.scope.RequestScopeUtil;
import com.springboot.craftkit.framework.scope.ScopeAttribute;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.servlet.filter.OrderedFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Filter for handling X-Forwarded-For and X-Forwarded-Service headers.
 * Registers forwarded information to RequestScopeAttribute.
 * <pre>
 * 1. X-Forwarded-For : IP addresses are added as requests pass through services
 * 2. X-Forwarded-Service : Service names are added as requests pass through services
 * </pre>
 */
@Setter
@Slf4j
public class XForwardedFilter implements OrderedFilter {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_FORWARDED_SERVICE = "X-Forwarded-Service";

    /**
     * <pre>
     * org.springframework.boot.web.servlet.filter.OrderedRequestContextFilter
     *   private int order = REQUEST_WRAPPER_FILTER_MAX_ORDER - 105;
     * Should execute after OrderedRequestContextFilter
     * OrderedRequestContextFilter -> GtidLogFilter -> XForwardedFilter -> ...
     * </pre>
     */
    int order = REQUEST_WRAPPER_FILTER_MAX_ORDER - 102;

    public XForwardedFilter() {
        super();
    }

    /**
     * Sets global attributes from HTTP request.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Set forwarded headers to scope attribute
        setScopeAttributeByHttpHeader(httpRequest);

        chain.doFilter(request, response);
    }

    /**
     * Sets scope attribute from HTTP headers.
     *
     * @param httpRequest HttpServletRequest
     */
    private void setScopeAttributeByHttpHeader(HttpServletRequest httpRequest) {
        try {
            ScopeAttribute scopeAttribute = (ScopeAttribute) RequestScopeUtil.getAttribute();

            // X-Forwarded-For
            String xForwardedFor = httpRequest.getHeader(X_FORWARDED_FOR);
            if (StringUtils.isNotBlank(xForwardedFor)) {
                List<String> forwardedForList = new ArrayList<>(Arrays.asList(xForwardedFor.split(",")));
                forwardedForList.replaceAll(String::trim);
                scopeAttribute.setForwardedFor(forwardedForList);
            }

            // X-Forwarded-Service
            String xForwardedService = httpRequest.getHeader(X_FORWARDED_SERVICE);
            if (StringUtils.isNotBlank(xForwardedService)) {
                List<String> forwardedServiceList = new ArrayList<>(Arrays.asList(xForwardedService.split(",")));
                forwardedServiceList.replaceAll(String::trim);
                scopeAttribute.setForwardedService(forwardedServiceList);
            }

            // Client IP from X-Forwarded-For or remote address
            String clientIp = httpRequest.getHeader(X_FORWARDED_FOR);
            if (StringUtils.isBlank(clientIp)) {
                clientIp = httpRequest.getRemoteAddr();
            } else {
                // Get first IP from comma-separated list
                clientIp = clientIp.split(",")[0].trim();
            }
            scopeAttribute.setClientIp(clientIp);

        } catch (Exception e) {
            log.warn("XForwardedFilter setScopeAttributeByHttpHeader error: {}", e.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return order;
    }

}
