package com.teststrategy.multimodule.maven.sf.framework.rest;

import com.teststrategy.multimodule.maven.sf.framework.rest.client.BusinessErrorDetectingInterceptor;
import com.teststrategy.multimodule.maven.sf.framework.rest.client.CircuitBreakerInterceptor;
import com.teststrategy.multimodule.maven.sf.framework.rest.config.RestAutoConfiguration;
import com.teststrategy.multimodule.maven.sf.framework.rest.config.RestCircuitBreakerAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

public class RestCircuitBreakerAutoConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RestAutoConfiguration.class, RestCircuitBreakerAutoConfiguration.class, RestTemplateAutoConfiguration.class));

    @Test
    void circuitBreakerInterceptor_wired_whenEnabled() {
        contextRunner
                .withPropertyValues(
                        "sample-framework.rest.circuitbreaker.enabled=true"
                )
                .run(context -> {
                    RestTemplate rt = context.getBean(RestTemplate.class);
                    assertThat(rt.getInterceptors()).anyMatch(i -> i instanceof CircuitBreakerInterceptor);
                });
    }

    @Test
    void interceptor_order_before_businessErrorHandler_whenBothEnabled() {
        contextRunner
                .withPropertyValues(
                        "sample-framework.rest.circuitbreaker.enabled=true",
                        "sample-framework.rest.error-handler.enabled=true"
                )
                .run(context -> {
                    RestTemplate rt = context.getBean(RestTemplate.class);
                    int cbIdx = indexOf(rt, CircuitBreakerInterceptor.class);
                    int bizIdx = indexOf(rt, BusinessErrorDetectingInterceptor.class);
                    assertThat(cbIdx).isNotNegative();
                    assertThat(bizIdx).isNotNegative();
                    assertThat(cbIdx).isLessThan(bizIdx);
                });
    }

    private static int indexOf(RestTemplate rt, Class<?> type) {
        for (int i = 0; i < rt.getInterceptors().size(); i++) {
            if (type.isInstance(rt.getInterceptors().get(i))) return i;
        }
        return -1;
    }
}
