package com.springboot.craftkit.framework.rest.setting;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RestTemplate를 위한 Resilience4j CircuitBreaker 통합을 활성화하고 조정하기 위한 설정.
 *
 * Prefix: sf-rest.circuitbreaker
 */
@ConfigurationProperties(prefix = SfRestCircuitBreakerProperties.PREFIX)
public class SfRestCircuitBreakerProperties {

    public static final String PREFIX = "sf-rest.circuitbreaker";

    /** CircuitBreaker 기능 활성화 여부. */
    private boolean enabled = false;

    /** 구현 모드 선택: INTERCEPTOR 또는 AOP(기본). */
    private Mode mode = Mode.AOP;

    /** 인스턴스 이름 지정 전략. */
    private InstanceFrom instanceFrom = InstanceFrom.DOMAIN_API;

    /** CircuitBreakerRegistry에 존재하는 선택적 기본 구성 이름. */
    private String defaultConfig;

    /** 무시할 예외 목록(CB 관점에서 성공으로 간주). FQCN. */
    private Set<String> ignoreExceptions = new LinkedHashSet<>(
            Arrays.asList("com.springboot.craftkit.sf.framework.rest.client.BusinessErrorException")
    );

    /** 실패로 기록할 예외 목록(기본 구성 외에). FQCN. */
    private Set<String> recordExceptions = new LinkedHashSet<>();

    public enum InstanceFrom { DOMAIN_API, URI }
    public enum Mode { INTERCEPTOR, AOP }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Mode getMode() { return mode; }
    public void setMode(Mode mode) { this.mode = mode; }

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
