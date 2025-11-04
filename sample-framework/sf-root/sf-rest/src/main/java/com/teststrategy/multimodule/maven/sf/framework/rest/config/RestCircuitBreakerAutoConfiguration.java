package com.teststrategy.multimodule.maven.sf.framework.rest.config;

import com.teststrategy.multimodule.maven.sf.framework.rest.client.BusinessErrorDetectingInterceptor;
import com.teststrategy.multimodule.maven.sf.framework.rest.client.CircuitBreakerInstanceNamer;
import com.teststrategy.multimodule.maven.sf.framework.rest.client.CircuitBreakerInterceptor;
import com.teststrategy.multimodule.maven.sf.framework.rest.setting.CircuitBreakerProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@AutoConfiguration
@ConditionalOnClass({RestTemplate.class, CircuitBreakerRegistry.class})
@EnableConfigurationProperties(CircuitBreakerProperties.class)
@ConditionalOnProperty(prefix = CircuitBreakerProperties.PREFIX, name = "enabled", havingValue = "true")
public class RestCircuitBreakerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        // Use default registry; users may also provide their own bean
        return CircuitBreakerRegistry.ofDefaults();
    }

    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerInstanceNamer circuitBreakerInstanceNamer(CircuitBreakerProperties props) {
        return new CircuitBreakerInstanceNamer(props);
    }

    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerInterceptor circuitBreakerInterceptor(CircuitBreakerRegistry registry,
                                                               CircuitBreakerProperties props,
                                                               CircuitBreakerInstanceNamer namer) {
        return new CircuitBreakerInterceptor(registry, props, namer);
    }

    @Bean
    @ConditionalOnProperty(prefix = CircuitBreakerProperties.PREFIX, name = "mode", havingValue = "INTERCEPTOR", matchIfMissing = false)
    public RestTemplateCustomizer circuitBreakerRestTemplateCustomizer(CircuitBreakerInterceptor interceptor,
                                                                       ObjectProvider<BusinessErrorDetectingInterceptor> businessInterceptorProvider) {
        return restTemplate -> {
            List var0 = restTemplate.getInterceptors();
            // Place CB before BusinessErrorDetectingInterceptor if present; else append
            int businessIdx = -1;
            for (int i = 0; i < var0.size(); i++) {
                if (businessInterceptorProvider.getIfAvailable() != null && var0.get(i) instanceof BusinessErrorDetectingInterceptor) {
                    businessIdx = i; break; }
            }
            if (businessIdx >= 0) {
                var0.add(businessIdx, interceptor);
            } else {
                var0.add(interceptor);
            }
        };
    }
}
