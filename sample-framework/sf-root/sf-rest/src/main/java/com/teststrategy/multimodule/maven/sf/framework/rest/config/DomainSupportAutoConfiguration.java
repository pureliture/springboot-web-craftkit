package com.teststrategy.multimodule.maven.sf.framework.rest.config;

import com.teststrategy.multimodule.maven.sf.framework.application.setting.DomainApiProperties;
import com.teststrategy.multimodule.maven.sf.framework.application.setting.DomainProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * Auto-configuration that exposes DomainProperties and DomainApiProperties beans to support URI template resolution
 */
@AutoConfiguration
@ConditionalOnClass({DomainProperties.class, DomainApiProperties.class})
public class DomainSupportAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = DomainProperties.CONFIG_PATH)
    public DomainProperties domainProperties(Environment environment) {
        return new DomainProperties(environment);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = DomainApiProperties.CONFIG_PATH)
    public DomainApiProperties domainApiProperties(Environment environment) {
        return new DomainApiProperties(environment);
    }
}
