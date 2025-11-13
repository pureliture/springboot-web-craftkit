package com.teststrategy.multimodule.maven.sf.framework.logging;

import com.teststrategy.multimodule.maven.sf.framework.application.setting.LoggingProperties;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Central configuration for logging AOP.
 */
@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(LoggingProperties.class)
public class LoggingConfiguration {

    @Bean
    @ConditionalOnProperty(name = "sample.log.auto-logging-enabled", havingValue = "true", matchIfMissing = false)
    public ExceptionLoggingAspect exceptionLoggingAspect(LoggingProperties properties) {
        return new ExceptionLoggingAspect(properties);
    }
}
