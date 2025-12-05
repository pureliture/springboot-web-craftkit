package com.springboot.craftkit.framework.request.filter;

import com.springboot.craftkit.framework.request.HttpRequestWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.filter.OrderedFilter;

import java.io.IOException;

/**
 * Filter that wraps HttpServletRequest with HttpRequestWrapper for request body caching.
 */
public class HttpRequestWrapperFilter implements OrderedFilter {

    /**
     * Http Request Wrapper Filter process
     *
     * @see jakarta.servlet.Filter#doFilter(jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse, jakarta.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpRequestWrapper requestWrapper = new HttpRequestWrapper(httpServletRequest);
        chain.doFilter(requestWrapper, response);
    }

    /**
     *
     * @see jakarta.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
        // none
    }

    @Override
    public int getOrder() {
        return REQUEST_WRAPPER_FILTER_MAX_ORDER;
    }
}
