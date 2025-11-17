package com.springboot.craftkit.framework.rest;

import com.springboot.craftkit.framework.rest.client.HmacClientHttpRequestInterceptor;
import com.springboot.craftkit.framework.rest.config.RestAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class RestHmacInterceptorTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RestAutoConfiguration.class, RestTemplateAutoConfiguration.class));

    @Test
    void hmacInterceptorAdded_whenHmacEnabled() {
        contextRunner
                .withPropertyValues(
                        "sf-rest.hmac.enabled=true",
                        "sf-rest.hmac.key-id=test-key",
                        "sf-rest.hmac.secret=test-secret"
                )
                .run(context -> {
                    RestTemplate rt = context.getBean(RestTemplate.class);
                    assertThat(rt.getInterceptors()).anyMatch(i -> i instanceof HmacClientHttpRequestInterceptor);
                });
    }
}
