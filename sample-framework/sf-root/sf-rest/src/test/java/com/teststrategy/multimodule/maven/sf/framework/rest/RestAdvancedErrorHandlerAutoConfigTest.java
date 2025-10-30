package com.teststrategy.multimodule.maven.sf.framework.rest;

import com.teststrategy.multimodule.maven.sf.framework.rest.client.BusinessErrorDetectingInterceptor;
import com.teststrategy.multimodule.maven.sf.framework.rest.config.RestAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

public class RestAdvancedErrorHandlerAutoConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RestAutoConfiguration.class, RestTemplateAutoConfiguration.class));

    @Test
    void businessErrorInterceptor_isWired_whenEnabled() {
        contextRunner
                .withPropertyValues(
                        "sample-framework.rest.error-handler.enabled=true"
                )
                .run(context -> {
                    RestTemplate rt = context.getBean(RestTemplate.class);
                    assertThat(rt.getInterceptors())
                            .anyMatch(i -> i instanceof BusinessErrorDetectingInterceptor);
                });
    }
}
