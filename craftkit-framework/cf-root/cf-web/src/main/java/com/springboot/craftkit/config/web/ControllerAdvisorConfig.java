package com.springboot.craftkit.config.web;

import com.springboot.craftkit.framework.aop.ControllerAdvisor;
import com.springboot.craftkit.framework.aop.ControllerAdvisorDecorateAspect;
import com.springboot.craftkit.framework.application.setting.HttpRequestWrapperProperties;
import com.springboot.craftkit.framework.request.filter.HttpRequestWrapperFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;

/**
 * Configuration for ControllerAdvisor support.
 * When a ControllerAdvisor bean exists, this configuration enables the aspect-based
 * controller interception functionality.
 */
@AutoConfiguration
@ConditionalOnWebApplication
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@ConditionalOnBean({ControllerAdvisor.class})
@AutoConfigureAfter(WebConfig.class)
@EnableAspectJAutoProxy
public class ControllerAdvisorConfig {

    /**
     * Enables HttpRequestWrapper to allow repeated reading of RequestBody
     * for common processing in ControllerAdvisor.
     *
     * @return FilterRegistrationBean for HttpRequestWrapperFilter
     */
    @Bean
    @ConditionalOnProperty(prefix = HttpRequestWrapperProperties.PREFIX, name = "enabled", havingValue = "false", matchIfMissing = true)
    public FilterRegistrationBean<HttpRequestWrapperFilter> httpRequestWrapperFilterRegistrationBeanForControllerAdvisor() {

        HttpRequestWrapperFilter filter = new HttpRequestWrapperFilter();
        FilterRegistrationBean<HttpRequestWrapperFilter> registrationBean = new FilterRegistrationBean<>(filter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(filter.getOrder());
        return registrationBean;
    }

    /**
     * Creates ControllerAdvisorDecorateAspect for AOP support on Controller methods
     * annotated with @GetMapping, @PutMapping, @PostMapping, @DeleteMapping, @RequestMapping.
     *
     * @param controllerAdvisorProvider ObjectProvider of ControllerAdvisor
     * @return ControllerAdvisorDecorateAspect
     */
    @Bean
    public ControllerAdvisorDecorateAspect controllerAdvisorDecorateAspect(ObjectProvider<ControllerAdvisor> controllerAdvisorProvider) {
        return new ControllerAdvisorDecorateAspect(controllerAdvisorProvider);
    }
}
