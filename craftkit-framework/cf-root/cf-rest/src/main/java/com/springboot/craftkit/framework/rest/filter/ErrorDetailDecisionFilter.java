package com.springboot.craftkit.framework.rest.filter;

import com.springboot.craftkit.framework.application.constant.HeaderConstant;
import com.springboot.craftkit.framework.application.setting.ErrorDetailProperties;
import com.springboot.craftkit.framework.error.ErrorDetailContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Decides whether error details should be exposed for the current request
 * based on X-Forwarded-Service (or configured header) and trusted service list.
 */
public class ErrorDetailDecisionFilter extends OncePerRequestFilter {

    private final ErrorDetailProperties properties;

    public ErrorDetailDecisionFilter(ErrorDetailProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        boolean enabled = decideEnabled(request);
        request.setAttribute(ErrorDetailContext.REQ_ATTR_ENABLED, enabled);
        ErrorDetailContext.setEnabled(enabled);
        try {
            filterChain.doFilter(request, response);
        } finally {
            ErrorDetailContext.clear();
        }
    }

    private boolean decideEnabled(HttpServletRequest request) {

        String headerValue = request.getHeader(HeaderConstant.HEADER_FORWARDED_SERVICE);

        if (headerValue == null || headerValue.trim().isEmpty()) {
            return properties.isDefaultEnabled();
        }

        String value = this.firstToken(headerValue);
        List<String> trusted = properties.getTrustedServices();

        if (trusted == null) {
            return properties.isDefaultEnabled();
        }

        for (String s : trusted) {
            if (s != null && value.equalsIgnoreCase(s.trim())) {
                return true;
            }
        }

        return properties.isDefaultEnabled();
    }

    private String firstToken(String headerValue) {
        String v = headerValue.trim();
        int idx = v.indexOf(',');
        if (idx > -1) {
            v = v.substring(0, idx);
        }
        return v.trim();
    }
}
