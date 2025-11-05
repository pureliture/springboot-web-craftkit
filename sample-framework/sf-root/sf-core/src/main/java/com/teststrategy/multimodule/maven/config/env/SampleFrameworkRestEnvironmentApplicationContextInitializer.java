package com.teststrategy.multimodule.maven.config.env;

import com.teststrategy.multimodule.maven.sf.framework.application.env.DomainPropertySourceLoadHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Loads domain.yml (if configured) into the Spring Environment early in application startup.
 *
 * Property key: sample-framework.rest.domain.config
 * - If it's a directory, a file named domain.yml will be attempted within that directory.
 * - If it's a file path or a resource path, it will be resolved accordingly.
 */
public class SampleFrameworkRestEnvironmentApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private static final Logger log = LoggerFactory.getLogger(SampleFrameworkRestEnvironmentApplicationContextInitializer.class);

    /**
     * Execute with high precedence, after Spring Cloud bootstrap config but before most user configs.
     */
    private int order = Ordered.HIGHEST_PRECEDENCE + 11;

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        try {
            new DomainPropertySourceLoadHelper(environment, applicationContext).load();
        } catch (Exception e) {
            log.debug("Domain properties loading skipped or failed: {}", e.getMessage());
        }
    }
}
