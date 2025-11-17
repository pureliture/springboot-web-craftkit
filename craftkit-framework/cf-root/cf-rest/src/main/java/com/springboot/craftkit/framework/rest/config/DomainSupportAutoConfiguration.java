package com.springboot.craftkit.framework.rest.config;

import com.springboot.craftkit.framework.rest.setting.DomainApiProperties;
import com.springboot.craftkit.framework.rest.setting.DomainProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

/**
 * Auto-configuration that exposes DomainProperties and DomainApiProperties beans to support URI template resolution
 */
@AutoConfiguration
@ConditionalOnClass({DomainProperties.class, DomainApiProperties.class})
public class DomainSupportAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = DomainProperties.CONFIG_PATH)
    public DomainProperties domainProperties(Environment environment, ResourceLoader resourceLoader) {
        return new DomainProperties(environment, resourceLoader);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = DomainApiProperties.CONFIG_PATH)
    public DomainApiProperties domainApiProperties(Environment environment, ResourceLoader resourceLoader) {
        return new DomainApiProperties(environment, resourceLoader);
    }
}
