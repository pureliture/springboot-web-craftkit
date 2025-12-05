package com.springboot.craftkit.framework.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.filter.OrderedFilter;

import java.io.IOException;

/**
 * actuator, swagger, h2-console 등 biz logic 제외 처리 필터
 */
public class StaticResourcesFilter implements OrderedFilter {

    int order = REQUEST_WRAPPER_FILTER_MAX_ORDER - 104;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        request.getRequestDispatcher(httpServletRequest.getServletPath()).forward(request, response);
    }

    /**
     * @param order the order to set
     */
    public final void setOrder(int order) {
        this.order = order;
    }

    /**
     * @return the order
     */
    @Override
    public final int getOrder() {
        return order;
    }

}
