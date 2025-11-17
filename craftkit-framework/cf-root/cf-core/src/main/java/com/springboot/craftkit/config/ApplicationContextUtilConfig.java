package com.springboot.craftkit.config;

import com.springboot.craftkit.framework.application.ApplicationContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ApplicationContextUtilConfig implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(ApplicationContextUtilConfig.class);

    public ApplicationContextUtilConfig(ApplicationContext applicationContext) {
        ApplicationContextUtil.setApplicationContext(applicationContext);
        log.info("ApplicationContextUtil configuratated");
    }
}
