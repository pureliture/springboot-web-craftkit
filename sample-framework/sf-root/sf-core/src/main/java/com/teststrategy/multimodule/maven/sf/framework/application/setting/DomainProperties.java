package com.teststrategy.multimodule.maven.sf.framework.application.setting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manages logical domain (host) URLs and optional bulk-service URLs from a YAML file.
 *
 * YAML format example:
 * services:
 *   demo:
 *     url: http://localhost:8081
 *   apim-pv:
 *     url: http://localhost:8081/apim
 *   apim-pb:
 *     url: http://localhost:8082/apim
 */
@Data
public class DomainProperties {

    private static final Logger log = LoggerFactory.getLogger(DomainProperties.class);

    /**
     * Property key to locate a YAML file or directory containing domain.yml
     */
    public static final String CONFIG_PATH = "sample-framework.rest.domain.config";

    /**
     * Optional prefix only for documentation/backward hints.
     */
    public static final String PREFIX = "sample-framework.rest.domain";

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

    /**
     * Logical service name -> ServiceProperties mapping
     */
    private Map<String, ServiceProperties> services = new LinkedHashMap<>();

    public DomainProperties(Environment environment) {
        this.environment = environment;
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

    private void loadFromYaml(String path) throws Exception {
        // Resolve to resource InputStream using Spring's ResourceLoader via Environment if possible
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream is = ResourceResolver.openStream(path)) {
            if (is == null) {
                log.debug("Domain YAML resource not found at {}", path);
                return;
            }
            JsonNode root = mapper.readTree(is);
            if (root == null || !root.has(SERVICES_KEY)) return;
            JsonNode servicesNode = root.get(SERVICES_KEY);
            Iterator<String> fieldNames = servicesNode.fieldNames();
            while (fieldNames.hasNext()) {
                String name = fieldNames.next();
                JsonNode svc = servicesNode.get(name);
                if (svc == null) continue;
                ServiceProperties sp = new ServiceProperties();
                JsonNode url = svc.get("url");
                if (url != null && !url.isNull()) sp.setUrl(url.asText());
                // support both bsvUrl and bsv-url
                JsonNode bsvUrl = svc.has("bsvUrl") ? svc.get("bsvUrl") : svc.get("bsv-url");
                if (bsvUrl != null && !bsvUrl.isNull()) sp.setBsvUrl(bsvUrl.asText());
                this.services.put(name, sp);
            }
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