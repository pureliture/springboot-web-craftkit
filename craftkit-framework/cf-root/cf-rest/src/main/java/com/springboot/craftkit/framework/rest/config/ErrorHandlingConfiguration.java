package com.springboot.craftkit.framework.rest.config;

import com.springboot.craftkit.framework.application.setting.ErrorDetailProperties;
import com.springboot.craftkit.framework.rest.filter.ErrorDetailDecisionFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Registers servlet filter deciding error detail exposure for each request.
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(ErrorDetailProperties.class)
public class ErrorHandlingConfiguration {

    @Bean
    @ConditionalOnProperty(name = "sample.error.filter-enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<ErrorDetailDecisionFilter> errorDetailDecisionFilter(ErrorDetailProperties properties) {
        FilterRegistrationBean<ErrorDetailDecisionFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ErrorDetailDecisionFilter(properties));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 100);
        registration.addUrlPatterns("/*");
        registration.setName("errorDetailDecisionFilter");
        return registration;
    }
}
