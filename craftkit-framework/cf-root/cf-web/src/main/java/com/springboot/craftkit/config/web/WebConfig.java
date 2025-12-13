package com.springboot.craftkit.config.web;

import com.springboot.craftkit.framework.application.setting.*;
import com.springboot.craftkit.framework.egov.filter.UrlRewriteFilter;
import com.springboot.craftkit.framework.filter.*;
import com.springboot.craftkit.framework.info.CraftkitGitInfoContributor;
import com.springboot.craftkit.framework.listener.ApplicationContextListener;
import com.springboot.craftkit.framework.request.filter.HttpRequestWrapperFilter;
import com.springboot.craftkit.framework.request.listener.ServletRequestListener;
import com.springboot.craftkit.framework.response.advice.CommonResponseAdvice;
import com.springboot.craftkit.framework.util.PropertyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import static org.springdoc.core.utils.Constants.SPRINGDOC_ENABLED;
import static org.springdoc.core.utils.Constants.SPRINGDOC_SWAGGER_UI_ENABLED;

/**
 * Web configuration for Craftkit framework.
 * Registers various filters, listeners, and beans for web application.
 */
@AutoConfiguration
@Slf4j
@ConditionalOnWebApplication
@EnableConfigurationProperties(
        value = {CorsFilterProperties.class, UrlRewriteFilterProperties.class, HttpRequestWrapperProperties.class,
                 RequestLoggingProperties.class, StaticResourcesFilterProperties.class})
public class WebConfig {

    /**
     * CORS Filter
     */
    @Bean
    @ConditionalOnProperty(prefix = CorsFilterProperties.PREFIX, name = "enabled", havingValue = "true")
    public FilterRegistrationBean<CorsFilter> corsFilterRegistrationBean(CorsFilterProperties corsFilterSetting) {

        String allowOrigin = corsFilterSetting.getAccessControlAllowOrigin();
        boolean allowCredentials = corsFilterSetting.isAccessControlAllowCredentials();
        String allowHeaders = corsFilterSetting.getAccessControlAllowHeaders();
        String allowMethods = corsFilterSetting.getAccessControlAllowMethods();
        int maxAge = corsFilterSetting.getAccessControlMaxAge();

        FilterRegistrationBean<CorsFilter> registrationBean = new FilterRegistrationBean<>(
                new CorsFilter(allowOrigin, allowCredentials, allowMethods, allowHeaders, maxAge));
        registrationBean.addUrlPatterns(corsFilterSetting.getUrlPatterns());
        log.info("craftkit cors-filter enabled!!!");
        return registrationBean;
    }

    /**
     * URL Rewrite Filter
     */
    @Bean
    @ConditionalOnProperty(prefix = UrlRewriteFilterProperties.PREFIX, name = "enabled",
            havingValue = "true")
    public FilterRegistrationBean<UrlRewriteFilter> urlRewriteFilterRegistrationBean(
            UrlRewriteFilterProperties urlRewriteFilterSetting) {
        return new FilterRegistrationBean<>(new UrlRewriteFilter(urlRewriteFilterSetting));
    }

    @Bean
    @ConditionalOnProperty(prefix = HttpRequestWrapperProperties.PREFIX, name = "enabled", havingValue = "true")
    public FilterRegistrationBean<HttpRequestWrapperFilter> httpRequestWrapperFilterRegistrationBean(
            HttpRequestWrapperProperties httpRequestWrapperSetting) {

        HttpRequestWrapperFilter filter = new HttpRequestWrapperFilter();
        FilterRegistrationBean<HttpRequestWrapperFilter> registrationBean =
                new FilterRegistrationBean<>(filter);
        registrationBean.addUrlPatterns(httpRequestWrapperSetting.getUrlPatterns());
        registrationBean.setOrder(filter.getOrder());
        return registrationBean;
    }

    /**
     * GTID Filter - Sets X-Global-Transaction-ID (always enabled)
     */
    @Bean
    public FilterRegistrationBean<GtidLogFilter> gtidFilterRegistrationBean() {
        GtidLogFilter filter = new GtidLogFilter();
        FilterRegistrationBean<GtidLogFilter> registrationBean = new FilterRegistrationBean<>(filter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(filter.getOrder());
        return registrationBean;
    }

    /**
     * Request listener
     */
    @Bean
    @ConditionalOnProperty(name = "craftkit.servlet-request-listener-enabled", havingValue = "true")
    public ServletListenerRegistrationBean<ServletRequestListener> servletRequestListener() {
        ServletListenerRegistrationBean<ServletRequestListener> listenerRegBean =
                new ServletListenerRegistrationBean<>();
        listenerRegBean.setListener(new ServletRequestListener());
        return listenerRegBean;
    }

    /**
     * X-Forwarded-For, X-Forwarded-Service filter.
     * Stores forwarded headers and adds hop information for rest or async calls.
     */
    @Bean
    public FilterRegistrationBean<XForwardedFilter> xForwardedFilterRegistrationBean() {

        XForwardedFilter xForwardedFilter = new XForwardedFilter();
        FilterRegistrationBean<XForwardedFilter> registrationBean =
                new FilterRegistrationBean<>(xForwardedFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(xForwardedFilter.getOrder());
        return registrationBean;
    }

    /**
     * Request log filter
     */
    @Bean
    @ConditionalOnProperty(prefix = RequestLoggingProperties.PREFIX, name = "enabled", havingValue = "true")
    public CommonsRequestLoggingFilter logFilter(RequestLoggingProperties requestLoggingProperties) {
        CommonsRequestLoggingFilter filter = new CommonLoggingFilter(requestLoggingProperties);
        filter.setBeforeMessagePrefix(requestLoggingProperties.getBeforeMessagePrefix());
        filter.setBeforeMessageSuffix(requestLoggingProperties.getBeforeMessageSuffix());
        filter.setIncludeHeaders(requestLoggingProperties.isIncludeHeaders());
        filter.setIncludeQueryString(requestLoggingProperties.isIncludeQueryString());
        filter.setIncludePayload(requestLoggingProperties.isIncludePayload());
        filter.setIncludeClientInfo(requestLoggingProperties.isIncludeClientInfo());
        filter.setMaxPayloadLength(requestLoggingProperties.getMaxPayloadLength());
        filter.setAfterMessagePrefix(requestLoggingProperties.getAfterMessagePrefix());
        filter.setAfterMessageSuffix(requestLoggingProperties.getAfterMessageSuffix());
        filter.setHeaderPredicate(requestLoggingProperties.getHeaderPredicate());
        return filter;
    }

    /**
     * Static resources filter for excluding actuator, swagger, etc. from business filters.
     *
     * @param filterSettings static resources filter properties
     * @return static resources filter registration bean
     */
    @Bean
    public FilterRegistrationBean<StaticResourcesFilter> ignoreFilterChainsFilterRegistrationBean(
            StaticResourcesFilterProperties filterSettings, Environment environment) {

        // actuator default
        filterSettings.addUrl("/actuator/*");

        // springdoc.api-docs.enabled == true && springdoc.swagger-ui.enabled == true -> swagger ui resource
        if (!PropertyUtil.hasAnyProfileByEndWith("prod") &&
                environment.getProperty(SPRINGDOC_ENABLED, boolean.class, true) &&
                environment.getProperty(SPRINGDOC_SWAGGER_UI_ENABLED, boolean.class, true)) {
            filterSettings.addUrl("/swagger-ui.html");
            filterSettings.addUrl("/v2/api-docs");
            filterSettings.addUrl("/swagger-ui");
            filterSettings.addUrl("/swagger-ui/*");
            filterSettings.addUrl("/v3/api-docs");
            filterSettings.addUrl("/v3/api-docs/*");
        }

        StaticResourcesFilter ignoreFilterChainsFilter = new StaticResourcesFilter();
        FilterRegistrationBean<StaticResourcesFilter> registrationBean = new FilterRegistrationBean<>(ignoreFilterChainsFilter);
        registrationBean.setUrlPatterns(filterSettings.getUrls());
        registrationBean.setOrder(ignoreFilterChainsFilter.getOrder());
        return registrationBean;
    }

    /**
     * URL Handler Filter for trailing slash handling.
     * Treats URLs with and without trailing slash (`/`) the same.
     *
     * <p>Since Spring Framework 6.0, URLs with trailing slash are distinguished by default.
     * This filter wraps requests so that `/path/to` and `/path/to/` are treated equally.
     *
     * @return FilterRegistrationBean for trailing slash handling
     */
    @Bean
    public FilterRegistrationBean<UrlHandlerFilter> urlHandlerFilter() {
        UrlHandlerFilter filter = UrlHandlerFilter
                .trailingSlashHandler("/**")
                .wrapRequest()
                .build();
        return new FilterRegistrationBean<>(filter);
    }

    @Bean
    @ConditionalOnBean(GitCommitProperties.class)
    public CraftkitGitInfoContributor gitInfoContributor(GitCommitProperties gitCommitProperties) {
        return new CraftkitGitInfoContributor(gitCommitProperties);
    }

    @Bean
    public <T> CommonResponseAdvice<T> commonResponseAdvice() {
        return new CommonResponseAdvice<>();
    }

    @Bean
    public ApplicationContextListener applicationContextListener() {
        return new ApplicationContextListener();
    }

}
