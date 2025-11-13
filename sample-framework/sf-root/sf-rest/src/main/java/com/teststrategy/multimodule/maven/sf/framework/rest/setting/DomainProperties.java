package com.teststrategy.multimodule.maven.sf.framework.rest.setting;

import com.teststrategy.multimodule.maven.sf.framework.properties.YamlResourcePropertySource;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.EncodedResource;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manages logical domain (host) URLs and optional bulk-service URLs from a YAML file.
 *
 * YAML format example:
 * services:
 *   demo:
 *     url: http://localhost:8081
 */
@Data
public class DomainProperties {

    private static final Logger log = LoggerFactory.getLogger(DomainProperties.class);

    /**
     * Property key to locate a YAML file or directory containing domain.yml
     */
    public static final String CONFIG_PATH = "sf-rest.domain.config";

    /**
     * Optional prefix only for documentation/backward hints.
     */
    public static final String PREFIX = "sf-rest.domain";

    /**
     * Services root key inside YAML.
     */
    public static final String SERVICES_KEY = "services";

    /**
     * Location provided by Environment; kept for toString() and debugging.
     */
    private String config;

    @ToString.Exclude
    private transient Environment environment;

    @ToString.Exclude
    private transient ResourceLoader resourceLoader;

    /**
     * Logical service name -> ServiceProperties mapping
     */
    private Map<String, ServiceProperties> services = new LinkedHashMap<>();

    public DomainProperties(Environment environment) {
        this(environment, new org.springframework.core.io.DefaultResourceLoader());
    }

    public DomainProperties(Environment environment, ResourceLoader resourceLoader) {
        this.environment = environment;
        this.resourceLoader = (resourceLoader != null) ? resourceLoader : new org.springframework.core.io.DefaultResourceLoader();
        if (environment != null) {
            String resolved = environment.getProperty(CONFIG_PATH);
            this.config = resolved;
            // best-effort load; missing file is tolerated
            if (StringUtils.isNotBlank(resolved)) {
                try {
                    loadFromYaml(resolved);
                } catch (Exception e) {
                    log.warn("Failed to load domain YAML from {}: {}", resolved, e.getMessage());
                }
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
        if (isBulkRequest && service.hasBsv()) {
            return service.getBsvUrl();
        }
        return service.getUrl();
    }

    private Resource resolveResource(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                return new FileSystemResource(file);
            }
        } catch (Exception ignored) {
        }
        try {
            if (this.resourceLoader != null) {
                Resource res = this.resourceLoader.getResource(path);
                if (res != null && res.exists()) {
                    return res;
                }
            }
        } catch (Exception ignored) {
        }
        return new ClassPathResource(path);
    }

    private void loadFromYaml(String path) throws Exception {
        Resource resource = resolveResource(path);
        if (!resource.exists()) {
            // if path is a directory, try domain.yml inside it
            File f = new File(path);
            if (f.isDirectory()) {
                resource = new FileSystemResource(new File(f, "domain.yml"));
            }
        }
        if (!resource.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("Domain YAML resource not found at {}", path);
            }
            return;
        }
        YamlResourcePropertySource ps = new YamlResourcePropertySource("domain", new EncodedResource(resource));
        String[] names = ps.getPropertyNames();
        if (names == null || names.length == 0) return;
        // parse properties like services.demo.url, services.demo.bsvUrl
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
            } else if ("bsvUrl".equals(prop) || "bsv-url".equals(prop)) {
                sp.setBsvUrl(value);
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
        private String bsvUrl;

        public boolean hasBsv() {
            return StringUtils.isNotBlank(this.bsvUrl);
        }

        @Override
        public String toString() {
            return "{svc=" + this.url + (this.hasBsv() ? (", bsv=" + this.bsvUrl) : "") + "}";
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