package com.teststrategy.multimodule.maven.sf.framework.rest;

import com.teststrategy.multimodule.maven.sf.framework.rest.config.DomainSupportAutoConfiguration;
import com.teststrategy.multimodule.maven.sf.framework.rest.config.RestAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DomainUriTemplateHandlerTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    RestAutoConfiguration.class,
                    DomainSupportAutoConfiguration.class,
                    RestTemplateAutoConfiguration.class
            ));

    @Test
    void expands_domainApi_and_domain_placeholders() {
        contextRunner
                .withPropertyValues(
                        "sample-framework.rest.domain.config=classpath:config/domain.yml",
                        "sample-framework.rest.domain.api.config=classpath:config/domain-api.yml"
                )
                .run(context -> {
                    RestTemplate rt = context.getBean(RestTemplate.class);
                    URI uri = rt.getUriTemplateHandler().expand("{@apim.resource}", Map.of("statusCode", "200"));
                    assertThat(uri.toString()).isEqualTo("http://localhost:8081/apim/resource?statusCode=200");
                });
    }

    @Test
    void resolves_environment_placeholders_in_uri() {
        contextRunner
                .withPropertyValues(
                        "sample-framework.rest.domain.config=classpath:config/domain.yml",
                        "test.value=hello"
                )
                .run(context -> {
                    RestTemplate rt = context.getBean(RestTemplate.class);
                    URI uri = rt.getUriTemplateHandler().expand("http://example.org/ping?x=${test.value}");
                    assertThat(uri.toString()).isEqualTo("http://example.org/ping?x=hello");
                });
    }
}
