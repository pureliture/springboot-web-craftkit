package com.springboot.craftkit.framework.request;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Utility class for holding HttpServletRequest in a ThreadLocal.
 */
public class ServletRequestHolder {

    private ServletRequestHolder() {
        super();
    }

    private static ThreadLocal<HttpServletRequest> holder = new ThreadLocal<>();

    public static HttpServletRequest getServletRequest() {
        HttpServletRequest httpServletRequest = holder.get();
        if (httpServletRequest == null) {
            return getRequest();
        }
        return httpServletRequest;
    }

    public static void hold(HttpServletRequest req) {
        holder.set(req);
    }

    public static void unhold() {
        holder.remove();
    }

    public static HttpServletRequest getRequest() {

        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (servletRequestAttributes == null)
            return null;

        return servletRequestAttributes.getRequest();
    }
}
