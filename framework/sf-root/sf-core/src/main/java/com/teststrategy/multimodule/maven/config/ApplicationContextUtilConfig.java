package com.teststrategy.multimodule.maven.config;

import com.teststrategy.multimodule.maven.sf.framework.application.ApplicationContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ApplicationContextUtilConfig {

    private static final Logger log = LoggerFactory.getLogger(ApplicationContextUtilConfig.class);

    public ApplicationContextUtilConfig(ApplicationContext applicationContext) {
        ApplicationContextUtil.setApplicationContext(applicationContext);
        log.info("ApplicationContextUtil configuratated");
    }
}
