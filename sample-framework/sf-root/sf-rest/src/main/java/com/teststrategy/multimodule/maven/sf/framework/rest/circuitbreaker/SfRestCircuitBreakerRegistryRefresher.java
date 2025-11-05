package com.teststrategy.multimodule.maven.sf.framework.rest.circuitbreaker;

import com.teststrategy.multimodule.maven.sf.framework.application.setting.DomainApiProperties;
import com.teststrategy.multimodule.maven.sf.framework.rest.setting.SfRestCircuitBreakerProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;

/**
 * Minimal registry refresher to pre-create circuit breaker instances for known domain.api pairs
 * when environment properties change (e.g., config refresh).
 */
public class SfRestCircuitBreakerRegistryRefresher implements ApplicationListener<EnvironmentChangeEvent> {

    private final CircuitBreakerRegistry registry;
    private final SfRestCircuitBreakerProperties sfProps;
    private final DomainApiProperties domainApiProperties;

    public SfRestCircuitBreakerRegistryRefresher(CircuitBreakerRegistry registry,
                                                 SfRestCircuitBreakerProperties sfProps,
                                                 DomainApiProperties domainApiProperties) {
        this.registry = registry;
        this.sfProps = sfProps;
        this.domainApiProperties = domainApiProperties;
    }

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        if (!sfProps.isEnabled()) return;
        domainApiProperties.getAllApis().forEach(api -> {
            String name = api.getDomain() + "." + api.getApi();
            try {
                // Will create or return existing instance
                registry.circuitBreaker(name);
            } catch (Exception ignore) { }
        });
    }
}
