package com.springboot.craftkit.framework.interceptor;

import com.springboot.craftkit.framework.application.ApplicationContextUtil;
import com.springboot.craftkit.framework.application.setting.RequestLoggingProperties;
import com.springboot.craftkit.framework.scope.RequestScopeUtil;
import com.springboot.craftkit.framework.scope.ScopeAttribute;
import com.springboot.craftkit.framework.util.HttpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Interceptor for handling audit attributes from XForwardedFilter
 * and supplementary processing for unhandled cases.
 */
public class AuditInterceptor implements HandlerInterceptor {

    @SuppressWarnings({"java:S3416", "LoggerInitializedWithForeignClass"})
    static final Logger loggerCommonLoggingFilter =
            LoggerFactory.getLogger(com.springboot.craftkit.framework.filter.CommonLoggingFilter.class);

    public AuditInterceptor() {
        super();
    }

    /**
     * Interceptor for storing audit items.
     *
     * @see HandlerInterceptor#preHandle(HttpServletRequest, HttpServletResponse, Object)
     */
    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object object) {

        String path = request.getRequestURI();
        if (HttpUtil.isCommonEndpointPatternContains(path))
            return true;

        setProgramId(request);

        logCommonsRequestLoggingFilter();

        return true;
    }

    /**
     * Write RequestScopeAttribute together when client info is written to CommonsRequestLoggingFilter logger.
     */
    private static void logCommonsRequestLoggingFilter() {
        if (loggerCommonLoggingFilter.isDebugEnabled()) {
            try {
                RequestLoggingProperties setting =
                        (RequestLoggingProperties) ApplicationContextUtil.getBean(RequestLoggingProperties.class);
                if (setting.isEnabled() && setting.isIncludeClientInfo()) {
                    loggerCommonLoggingFilter.debug("{}", RequestScopeUtil.getAttribute());
                }
            } catch (Exception e) {
                loggerCommonLoggingFilter
                        .warn("AuditInterceptor userinfo log writing error : {}".concat(e.getMessage()));
            }
        }
    }

    /**
     * Supplement if RequestScopeUtil.getAttribute().getProgramId() value is missing.
     * <p>
     * If program id is empty:
     * - Use format: (http-method)::(uri-template)
     */
    public void setProgramId(HttpServletRequest request) {

        ScopeAttribute scopeAttribute = (ScopeAttribute) RequestScopeUtil.getAttribute();
        String programId = scopeAttribute.getProgramId();

        // not empty
        if (StringUtils.isNotBlank(programId) && !ScopeAttribute.EMPTY.equals(programId)) {
            return;
        }

        // 1. Get controller mapping
        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        // 2. Get http method
        String method = request.getMethod();
        // Limit to 49 bytes for DB storage
        int maxLength = 49;
        String newProgramId = method + ":" + StringUtils.abbreviate(path != null ? path : request.getRequestURI(), maxLength - method.length() - 1);

        scopeAttribute.setProgramId(newProgramId);
    }

}
