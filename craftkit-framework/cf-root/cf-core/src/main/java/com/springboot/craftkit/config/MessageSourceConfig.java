package com.springboot.craftkit.config;

import com.springboot.craftkit.framework.application.setting.MessageProperties;
import com.springboot.craftkit.framework.message.accessor.FileMessageSourceAccessor;
import com.springboot.craftkit.framework.message.accessor.MessageSourceAccessor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;


@Configuration
@EnableConfigurationProperties(value = MessageProperties.class)
public class MessageSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(MessageSourceConfig.class);

    @Bean
    @ConditionalOnMissingBean(AbstractMessageSource.class)
    public ReloadableResourceBundleMessageSource messageFileSource(MessageProperties messageSetting) {

        log.debug("message source setting : {}", messageSetting);

        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();

        if (!StringUtils.isEmpty(messageSetting.getBasename())) {
            messageSource.setBasename(messageSetting.getBasename());
        } else {
            messageSource.setBasenames(messageSetting.getBasenames());
        }
        messageSource.setDefaultEncoding(messageSetting.getEncoding());
        messageSource.setCacheSeconds(messageSetting.getCacheSeconds());
        messageSource.setUseCodeAsDefaultMessage(true);

        return messageSource;
    }

    @Bean(name = "messageSourceAccessor")
    @ConditionalOnMissingBean(MessageSourceAccessor.class)
    public MessageSourceAccessor messageSourceFileAccessor(ReloadableResourceBundleMessageSource messageSource) {

        MessageSourceAccessor messageSourceAccessor = new FileMessageSourceAccessor(messageSource);
        log.debug("messageSourceFileAccessor is loaded {}", messageSource);
        return messageSourceAccessor;
    }
}
