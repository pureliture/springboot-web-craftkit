package com.springboot.craftkit.config;

import com.springboot.craftkit.framework.application.setting.LoggingProperties;
import com.springboot.craftkit.framework.logging.ExceptionLoggingAspect;
import com.springboot.craftkit.framework.util.MessageUtil;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(LoggingProperties.class)
public class LoggingConfig {

    private static final Logger log = LoggerFactory.getLogger(LoggingConfig.class);

    private final LoggingProperties loggingProperties;

    public LoggingConfig(LoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    @PostConstruct
    void init(){
        MessageUtil.initUseAbbreviatedName(loggingProperties.isUseAbbreviatedPackageName());
    }

    @Bean
    @ConditionalOnProperty(name = "sample.log.auto-logging-enabled", havingValue = "true", matchIfMissing = false)
    public ExceptionLoggingAspect exceptionLoggingAspect(LoggingProperties properties) {
        return new ExceptionLoggingAspect(properties);
    }
}
