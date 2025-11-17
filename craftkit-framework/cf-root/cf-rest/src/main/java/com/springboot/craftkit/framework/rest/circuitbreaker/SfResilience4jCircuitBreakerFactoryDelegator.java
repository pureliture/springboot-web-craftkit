package com.springboot.craftkit.framework.rest.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigurationProperties;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

/**
 * Delegates creation of Spring Cloud {@link org.springframework.cloud.client.circuitbreaker.CircuitBreaker}
 * to a configured {@link Resilience4JCircuitBreakerFactory}
 */
public class SfResilience4jCircuitBreakerFactoryDelegator {

    private final Resilience4JCircuitBreakerFactory factory;

    public SfResilience4jCircuitBreakerFactoryDelegator(CircuitBreakerRegistry circuitBreakerRegistry,
                                                        TimeLimiterRegistry timeLimiterRegistry,
                                                        Resilience4JConfigurationProperties cloudProps) {
        this.factory = new Resilience4JCircuitBreakerFactory(circuitBreakerRegistry, timeLimiterRegistry, null);

        if (cloudProps != null) {
            Resilience4JConfigurationProperties sync = new Resilience4JConfigurationProperties();
            sync.setDisableTimeLimiter(true);
            sync.setDisableThreadPool(true);
            sync.setDefaultGroupTag(cloudProps.getDefaultGroupTag());
            sync.setEnableGroupMeterFilter(cloudProps.isEnableGroupMeterFilter());
            sync.setEnableSemaphoreDefaultBulkhead(cloudProps.isEnableSemaphoreDefaultBulkhead());
            // In a full implementation we would apply group/instance config mapping here as needed
        }
    }

    public CircuitBreakerFactory<?, ?> getFactory() {
        return this.factory;
    }
}
