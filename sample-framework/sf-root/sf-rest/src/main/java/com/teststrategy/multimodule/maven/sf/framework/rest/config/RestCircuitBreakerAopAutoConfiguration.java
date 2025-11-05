package com.teststrategy.multimodule.maven.sf.framework.rest.config;

import com.teststrategy.multimodule.maven.sf.framework.application.setting.DomainApiProperties;
import com.teststrategy.multimodule.maven.sf.framework.rest.circuitbreaker.RestTemplateCircuitBreakerAspect;
import com.teststrategy.multimodule.maven.sf.framework.rest.circuitbreaker.SfResilience4jCircuitBreakerFactoryDelegator;
import com.teststrategy.multimodule.maven.sf.framework.rest.circuitbreaker.SfRestCircuitBreakerRegistryRefresher;
import com.teststrategy.multimodule.maven.sf.framework.rest.client.CircuitBreakerInstanceNamer;
import com.teststrategy.multimodule.maven.sf.framework.rest.setting.SfRestCircuitBreakerProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
@ConditionalOnClass({RestTemplate.class, CircuitBreakerRegistry.class})
@org.springframework.boot.context.properties.EnableConfigurationProperties(SfRestCircuitBreakerProperties.class)
@ConditionalOnProperty(prefix = SfRestCircuitBreakerProperties.PREFIX, name = "mode", havingValue = "AOP")
public class RestCircuitBreakerAopAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }

    @Bean
    @ConditionalOnMissingBean
    public TimeLimiterRegistry timeLimiterRegistry() {
        return TimeLimiterRegistry.ofDefaults();
    }

    @Bean
    @ConditionalOnMissingBean
    public SfResilience4jCircuitBreakerFactoryDelegator sfResilience4jCircuitBreakerFactoryDelegator(
            CircuitBreakerRegistry registry,
            TimeLimiterRegistry timeLimiterRegistry,
            org.springframework.beans.factory.ObjectProvider<Resilience4JConfigurationProperties> cloudProps) {
        return new SfResilience4jCircuitBreakerFactoryDelegator(registry, timeLimiterRegistry, cloudProps.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = SfRestCircuitBreakerProperties.PREFIX, name = "enabled", havingValue = "true")
    public RestTemplateCircuitBreakerAspect restTemplateCircuitBreakerAspect(SfResilience4jCircuitBreakerFactoryDelegator delegator,
                                                                             SfRestCircuitBreakerProperties properties,
                                                                             CircuitBreakerInstanceNamer namer) {
        return new RestTemplateCircuitBreakerAspect(delegator.getFactory(), properties, namer);
    }

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnBean(DomainApiProperties.class)
    @ConditionalOnProperty(prefix = SfRestCircuitBreakerProperties.PREFIX, name = "enabled", havingValue = "true")
    public SfRestCircuitBreakerRegistryRefresher sfRestCircuitBreakerRegistryRefresher(CircuitBreakerRegistry registry,
                                                                                       SfRestCircuitBreakerProperties properties,
                                                                                       DomainApiProperties dap) {
        return new SfRestCircuitBreakerRegistryRefresher(registry, properties, dap);
    }
}
