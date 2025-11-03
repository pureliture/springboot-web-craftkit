package com.teststrategy.multimodule.maven.sf.framework.rest.setting;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Properties to enable and tune Resilience4j CircuitBreaker integration for RestTemplate.
 *
 * Prefix: sample-framework.rest.circuitbreaker
 */
@ConfigurationProperties(prefix = CircuitBreakerProperties.PREFIX)
public class CircuitBreakerProperties {

    public static final String PREFIX = "sample-framework.rest.circuitbreaker";

    /** Enable CircuitBreaker feature. */
    private boolean enabled = false;

    /** Instance naming strategy. */
    private InstanceFrom instanceFrom = InstanceFrom.DOMAIN_API;

    /** Optional base configuration name present in CircuitBreakerRegistry. */
    private String defaultConfig;

    /** Exceptions to ignore (treated as success from CB perspective). FQCNs. */
    private Set<String> ignoreExceptions = new LinkedHashSet<>(
            Arrays.asList("com.teststrategy.multimodule.maven.sf.framework.rest.client.BusinessErrorException")
    );

    /** Exceptions to record as failures (in addition to base config). FQCNs. */
    private Set<String> recordExceptions = new LinkedHashSet<>();

    public enum InstanceFrom { DOMAIN_API, URI }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public InstanceFrom getInstanceFrom() { return instanceFrom; }
    public void setInstanceFrom(InstanceFrom instanceFrom) { this.instanceFrom = instanceFrom; }

    public String getDefaultConfig() { return defaultConfig; }
    public void setDefaultConfig(String defaultConfig) { this.defaultConfig = defaultConfig; }

    public Set<String> getIgnoreExceptions() { return ignoreExceptions; }
    public void setIgnoreExceptions(Set<String> ignoreExceptions) { this.ignoreExceptions = normalize(ignoreExceptions); }

    public Set<String> getRecordExceptions() { return recordExceptions; }
    public void setRecordExceptions(Set<String> recordExceptions) { this.recordExceptions = normalize(recordExceptions); }

    private static Set<String> normalize(Set<String> input) {
        if (input == null) return null;
        return input.stream().filter(s -> s != null).map(String::trim).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
