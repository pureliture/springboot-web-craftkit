package com.springboot.craftkit.framework.rest;

import com.springboot.craftkit.framework.rest.config.RestAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpClient5RetryAutoConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RestAutoConfiguration.class, RestTemplateAutoConfiguration.class));

    @Test
    void httpClient5Retry_enabled_switchesRequestFactory() {
        contextRunner
                .withPropertyValues(
                        "sf-rest.http-client.retry.enabled=true",
                        "sf-rest.http-client.connect-timeout=1s",
                        "sf-rest.http-client.read-timeout=2s"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(RestTemplate.class);
                    RestTemplate rt = context.getBean(RestTemplate.class);
                    ClientHttpRequestFactory f = rt.getRequestFactory();
                    ClientHttpRequestFactory delegate = unwrapFactory(f);
                    assertThat(delegate).isInstanceOf(HttpComponentsClientHttpRequestFactory.class);
                });
    }

    private static ClientHttpRequestFactory unwrapFactory(ClientHttpRequestFactory f) {
        ClientHttpRequestFactory current = f;
        int guard = 5; // avoid infinite loops
        while (guard-- > 0 && current instanceof InterceptingClientHttpRequestFactory) {
            Object inner = reflectInnerFactory(current);
            if (inner instanceof ClientHttpRequestFactory cf) {
                current = cf;
            } else {
                break;
            }
        }
        return current;
    }

    private static Object reflectInnerFactory(Object wrapper) {
        Class<?> c = wrapper.getClass();
        while (c != null) {
            for (java.lang.reflect.Field field : c.getDeclaredFields()) {
                if (ClientHttpRequestFactory.class.isAssignableFrom(field.getType())) {
                    try {
                        field.setAccessible(true);
                        return field.get(wrapper);
                    } catch (Exception ignored) {}
                }
            }
            c = c.getSuperclass();
        }
        return null;
    }
}
