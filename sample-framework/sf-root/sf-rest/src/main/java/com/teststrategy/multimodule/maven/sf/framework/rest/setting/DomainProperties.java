package com.teststrategy.multimodule.maven.sf.framework.rest.setting;

import com.teststrategy.multimodule.maven.sf.framework.properties.YamlResourcePropertySource;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.*;
import org.springframework.core.io.support.EncodedResource;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * YAML 파일에서 논리 도메인(호스트) URL을 로드/관리합니다.
 *
 * YAML 형식 예:
 * services:
 *   demo:
 *     url: http://localhost:8081
 */
@Data
public class DomainProperties {

    private static final Logger log = LoggerFactory.getLogger(DomainProperties.class);

    /**
     * domain.yml을 포함하는 디렉터리 또는 YAML 파일의 위치를 찾기 위한 프로퍼티 키
     */
    public static final String CONFIG_PATH = "sf-rest.domain.config";

    /**
     * 문서화/이전 호환 힌트용 선택적 접두사
     */
    public static final String PREFIX = "sf-rest.domain";

    /**
     * YAML 내 서비스 루트 키
     */
    public static final String SERVICES_KEY = "services";

    /**
     * Environment로부터 주어진 설정 위치(디버깅 및 toString용)
     */
    private String config;

    @ToString.Exclude
    private Environment environment;

    @ToString.Exclude
    private ResourceLoader resourceLoader;

    /**
     * 논리 서비스명 -> ServiceProperties 매핑
     */
    private Map<String, ServiceProperties> services = new LinkedHashMap<>();

    public DomainProperties(Environment environment) {
        this(environment, new DefaultResourceLoader());
    }

    public DomainProperties(Environment environment, ResourceLoader resourceLoader) {

        this.environment = environment;
        this.resourceLoader = (resourceLoader != null) ? resourceLoader : new DefaultResourceLoader();

        if (environment == null) {
            return;
        }

        String resolved = environment.getProperty(CONFIG_PATH);
        this.config = resolved;

        // 가능한 경우에만 로드; 파일 누락은 허용
        if (StringUtils.isNotBlank(resolved)) {
            try {
                this.loadFromYaml(resolved);
            } catch (Exception e) {
                log.warn("도메인 YAML을 {}에서 로드하지 못했습니다: {}", resolved, e.getMessage());
            }
        }
    }

    public String getConfigPath() {
        return this.config;
    }

    public boolean hasDomain(final String domainName) {
        try {
            return this.getServices().containsKey(domainName);
        } catch (final Exception e) {
            return false;
        }
    }

    public String getDomainUrl(final String domainName) {
        ServiceProperties sp = this.getServices().get(domainName);
        return sp == null ? null : sp.getUrl();
    }

    public String getDomainUrl(final String domainName, final boolean isBulkRequest) {
        final ServiceProperties service = this.getServices().get(domainName);
        if (service == null) return null;
        // bsvUrl 개념 제거: bulk 여부와 관계없이 기본 url만 반환
        return service.getUrl();
    }

    private Resource resolveResource(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                return new FileSystemResource(file);
            }
        } catch (Exception ignored) {
            // eat
        }

        try {
            if (this.resourceLoader != null) {
                Resource res = this.resourceLoader.getResource(path);
                if (res.exists()) {
                    return res;
                }
            }
        } catch (Exception ignored) {
            // eat
        }

        return new ClassPathResource(path);
    }

    private void loadFromYaml(String path) throws Exception {

        Resource resource = this.resolveResource(path);

        if (!resource.exists()) {
            // 경로가 디렉터리인 경우 내부의 domain.yml 시도
            File f = new File(path);
            if (f.isDirectory()) {
                resource = new FileSystemResource(new File(f, "domain.yml"));
            }
        }

        if (!resource.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("해당 위치에 도메인 YAML 리소스가 없습니다: {}", path);
            }
            return;
        }

        YamlResourcePropertySource ps = new YamlResourcePropertySource("domain", new EncodedResource(resource));
        String[] names = ps.getPropertyNames();

        if (ArrayUtils.isEmpty(names)) return;

        // services.demo.url 형태의 프로퍼티 파싱
        Map<String, ServiceProperties> map = new LinkedHashMap<>();
        for (String key : names) {
            if (!key.startsWith(SERVICES_KEY + ".")) continue;
            String remainder = key.substring((SERVICES_KEY + ".").length());
            int dot = remainder.indexOf('.');
            if (dot <= 0) continue;
            String name = remainder.substring(0, dot);
            String prop = remainder.substring(dot + 1);
            Object raw = ps.getProperty(key);
            String value = raw != null ? String.valueOf(raw) : null;
            ServiceProperties sp = map.computeIfAbsent(name, k -> new ServiceProperties());
            if ("url".equals(prop)) {
                sp.setUrl(value);
            }
        }

        if (!map.isEmpty()) {
            this.services.putAll(map);
        }
    }

    @Data
    @NoArgsConstructor
    public static class ServiceProperties {
        private String url;

        @Override
        public String toString() {
            return "{svc=" + this.url + "}";
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DomainProperties (")
                .append(this.config)
                .append(") [");
        this.services.forEach((key, service) -> sb.append('\n').append('\t').append(key).append(" = ").append(service));
        return sb.append('\n').append(']').toString();
    }
}