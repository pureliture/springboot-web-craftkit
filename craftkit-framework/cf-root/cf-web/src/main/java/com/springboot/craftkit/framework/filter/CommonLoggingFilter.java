package com.springboot.craftkit.framework.filter;

import com.springboot.craftkit.framework.application.setting.RequestLoggingProperties;
import com.springboot.craftkit.framework.util.HttpUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.io.IOException;


/**
 * org.springframework.web.filter.CommonsRequestLoggingFilter 의 url 제어를 위한 확장 class
 */
public class CommonLoggingFilter extends CommonsRequestLoggingFilter {

    final RequestLoggingProperties requestLoggingProperties;

    public CommonLoggingFilter(RequestLoggingProperties requestLoggingProperties) {
        super();
        this.requestLoggingProperties = requestLoggingProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI().replace(request.getContextPath(), "");

        return requestURI.startsWith("/actuator")
                || requestURI.startsWith("/swagger")
                || requestURI.startsWith("/v2/api-docs")
                || requestURI.startsWith("/v3/api-docs")
                ;
    }

    /**
     * CommonsRequestLoggingFilter 처리를 수행한다.
     * 단, exclude-url-pattern 을 정의하여 패턴이 일치하는 경우 로깅을 우회한다.
     *
     * @param request http servlet request
     * @param response http servlet response
     * @param filterChain filter chain
     * @see org.springframework.web.filter.AbstractRequestLoggingFilter#doFilterInternal(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, jakarta.servlet.FilterChain)
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (!HttpUtil.isPatternContains(path, requestLoggingProperties.getExcludeUrlPatterns())) {
            // logging
            super.doFilterInternal(request, response, filterChain);
        } else {
            // logging skip
            filterChain.doFilter(request, response);
        }

    }

}
