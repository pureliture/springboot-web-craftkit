package com.springboot.craftkit.framework.rest;

import com.springboot.craftkit.framework.rest.config.DomainSupportAutoConfiguration;
import com.springboot.craftkit.framework.rest.config.RestCircuitBreakerAopAutoConfiguration;
import com.springboot.craftkit.framework.rest.setting.DomainApiProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DomainApiPropertiesTest {

    private final ApplicationContextRunner context = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DomainSupportAutoConfiguration.class, RestCircuitBreakerAopAutoConfiguration.class));

    @Test
    void parses_optional_api_from_uri_template() {
        context
                .withPropertyValues("sf-rest.domain.api.config=classpath:config/domain-api.yml")
                .run(ctx -> {
                    DomainApiProperties dap = ctx.getBean(DomainApiProperties.class);
                    assertThat(dap.getOptionalApi("http://base/{@apim.resource}").isPresent()).isTrue();
                    assertThat(dap.getOptionalApi("http://base/{id}").isPresent()).isFalse();
                    assertThat(dap.getOptionalApi("http://base/{@notexists.any}").isPresent()).isFalse();
                });
    }

    @Test
    void resolves_environment_placeholders_inside_api_url() {
        context
                .withPropertyValues(
                        "sf-rest.domain.api.config=classpath:config/domain-api-env.yml",
                        "test.value=ok"
                )
                .run(ctx -> {
                    DomainApiProperties dap = ctx.getBean(DomainApiProperties.class);
                    String resolved = dap.getUri("http://base/{@env.test}");
                    assertThat(resolved).isEqualTo("http://base/{@env}" + "/v-ok");
                });
    }

    @Test
    void refresher_precreates_circuit_breakers_from_all_apis() {
        context
                .withPropertyValues(
                        "sf-rest.domain.api.config=classpath:config/domain-api.yml",
                        "sf-rest.circuitbreaker.enabled=true",
                        "sf-rest.circuitbreaker.mode=AOP"
                )
                .run(ctx -> {
                    CircuitBreakerRegistry registry = ctx.getBean(CircuitBreakerRegistry.class);
                    // publish environment change
                    ctx.publishEvent(new EnvironmentChangeEvent(ctx, Map.of("x","y").keySet()));
                    // should be able to get breakers for each api
                    DomainApiProperties dap = ctx.getBean(DomainApiProperties.class);
                    dap.getAllApis().forEach(api -> {
                        String name = api.getDomain() + "." + api.getApi();
                        assertThat(registry.circuitBreaker(name)).isNotNull();
                    });
                });
    }
}
