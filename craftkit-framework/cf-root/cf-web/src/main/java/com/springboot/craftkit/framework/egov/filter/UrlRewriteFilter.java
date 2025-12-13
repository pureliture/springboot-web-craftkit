package com.springboot.craftkit.framework.egov.filter;

import com.springboot.craftkit.framework.application.setting.UrlRewriteFilterProperties;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;

/**
 * URL Rewrite Filter for HTTP/HTTPS redirection based on URL patterns.
 */
public class UrlRewriteFilter implements Filter {

    private String targetURI;
    private String httpsPort;
    private String httpPort;

    private String[] uriPatterns;

    public UrlRewriteFilter(UrlRewriteFilterProperties config) {
        String delimiter = ",";

        this.targetURI = config.getTargetUri();
        this.httpsPort = config.getHttpsPort();
        this.httpPort = config.getHttpPort();

        this.uriPatterns = targetURI.split(delimiter);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String getProtocol = req.getScheme();
        String getDomain = req.getServerName();

        AntPathMatcher pm = new AntPathMatcher();

        for (String uriPattern : uriPatterns) {
            if (pm.match(uriPattern.trim(), uri)) {
                if (getProtocol.equalsIgnoreCase("http")) {
                    response.setContentType("text/html");
                    String httpsPath = "https" + "://" + getDomain + ":" + httpsPort + uri;
                    res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                    res.setHeader("Location", httpsPath);
                }

            } else if (getProtocol.equalsIgnoreCase("https")) {
                response.setContentType("text/html");
                String httpPath = "http" + "://" + getDomain + ":" + httpPort + uri;
                res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                res.setHeader("Location", httpPath);
            }
        }

        chain.doFilter(req, res);

    }

    @Override
    public void destroy() {
        this.targetURI = null;
        this.httpsPort = null;
        this.httpPort = null;
        this.uriPatterns = null;
    }
}
