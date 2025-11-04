package com.teststrategy.multimodule.maven.sf.framework.rest;

import com.teststrategy.multimodule.maven.sf.framework.rest.client.TokenClientHttpRequestInterceptor;
import com.teststrategy.multimodule.maven.sf.framework.rest.config.RestAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

public class RestAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RestAutoConfiguration.class, RestTemplateAutoConfiguration.class));

    @Test
    void restTemplateBeanCreated_withTimeouts() {
        contextRunner
                .withPropertyValues(
                        "sf-rest.http-client.connect-timeout=2s",
                        "sf-rest.http-client.read-timeout=3s"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(RestTemplate.class);
                    RestTemplate rt = context.getBean(RestTemplate.class);
                    assertThat(rt).isNotNull();
                });
    }

    @Test
    void tokenInterceptorAdded_whenOAuthEnabled() {
        contextRunner
                .withPropertyValues(
                        "sf-rest.oauth.enabled=true",
                        "sf-rest.oauth.static-token=dummy-token"
                )
                .run(context -> {
                    RestTemplate rt = context.getBean(RestTemplate.class);
                    assertThat(rt.getInterceptors()).anyMatch(i -> i instanceof TokenClientHttpRequestInterceptor);
                });
    }
}
