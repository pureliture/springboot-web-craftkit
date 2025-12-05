package com.springboot.craftkit.framework.request.listener;

import com.springboot.craftkit.framework.request.ServletRequestHolder;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Servlet request listener that holds and releases HttpServletRequest in ServletRequestHolder.
 */
public class ServletRequestListener implements jakarta.servlet.ServletRequestListener {

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        HttpServletRequest req = (HttpServletRequest) sre.getServletRequest();
        ServletRequestHolder.hold(req);
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        HttpServletRequest req = (HttpServletRequest) sre.getServletRequest();
        if (req != null)
            ServletRequestHolder.unhold();
    }

}
