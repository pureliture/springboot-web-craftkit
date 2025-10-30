package com.teststrategy.multimodule.maven.sf.framework.application.setting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.web.util.UriTemplate;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/**
 * Loads a YAML describing APIs per logical domain and helps resolve "{@domain.api}" placeholders
 * inside URI templates to concrete URLs.
 *
 * YAML example:
 * demo:
 *   get:
 *     url: /get?id={id}&name={name}
 * apim:
 *   resource:
 *     url: "{@apim-pv}/resource?statusCode={statusCode}"
 */
@Slf4j
public class DomainApiProperties implements InitializingBean {

    public static final String BULK_REQUEST_METHOD = ":bulkProcess";

    public static final String CONFIG_PATH = "sample-framework.rest.domain.api.config";
    public static final String PLACEHOLDERS = "${" + CONFIG_PATH + ":}";

    @ToString.Exclude
    private final transient Environment environment;

    private final String configPath;

    /**
     * domainName -> (apiName -> ApiProperties)
     */
    LinkedHashMap<String, LinkedHashMap<String, ApiProperties>> domainApis = new LinkedHashMap<>();

    public DomainApiProperties(final Environment environment) {
        this.environment = environment;
        String resolved = environment != null ? environment.resolvePlaceholders(PLACEHOLDERS) : null;
        this.configPath = StringUtils.defaultString(resolved);

        if (StringUtils.isBlank(this.configPath)) {
            log.debug("{} not configured; DomainApiProperties will be inactive.", CONFIG_PATH);
            throw new IllegalStateException(CONFIG_PATH + " is not configured");
        }

        try {
            loadFromYaml(this.configPath);
            // backfill domain/api names into each ApiProperties
            if (domainApis != null) {
                domainApis.forEach((domain, apis) -> apis.forEach((apiName, api) -> {
                    api.setDomain(domain);
                    api.setApi(apiName);
                }));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load domain-api YAML from " + this.configPath + ": " + e.getMessage(), e);
        }
    }

    public Optional<ApiProperties> getOptionalApi(final String uriTemplateString) {
        try {
            final UriTemplate uriTemplate = new UriTemplate(uriTemplateString);
            ApiProperties api = null;
            final Optional<String> variable = uriTemplate.getVariableNames()
                    .stream()
                    .filter(var -> var.startsWith("@") && var.matches("@[\\w-]+\\.[\\w-]+")) // @domain.api
                    .findFirst();

            if (variable.isPresent()) {
                final String[] splited = variable.get().split("\\.");
                final String atDomainName = splited[0];
                final String domainName = atDomainName.substring(1);
                final String apiName = splited[1];
                final LinkedHashMap<String, ApiProperties> apiList = this.domainApis.get(domainName);
                if (apiList != null) {
                    api = apiList.get(apiName);
                }
            }
            return Optional.ofNullable(api);
        } catch (final Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Get all ApiProperties entries.
     */
    public List<ApiProperties> getAllApis() {
        return this.domainApis.values().stream()
                .flatMap(innerMap -> innerMap.values().stream())
                .toList();
    }

    public String getConfigPath() {
        return this.configPath;
    }

    public String getUri(final String uriTemplateString) {
        final Optional<ApiProperties> apiOptional = this.getOptionalApi(uriTemplateString);
        if (apiOptional.isPresent()) {
            final ApiProperties api = apiOptional.get();
            String url = api.findUrlVariableAndReplace(uriTemplateString);
            url = this.replaceEnvironmentIfExist(url);
            return url;
        }
        return uriTemplateString;
    }

    private String replaceEnvironmentIfExist(String url) {
        if (this.environment != null && url.contains("${")) {
            url = this.environment.resolvePlaceholders(url);
        }
        return url;
    }

    private void loadFromYaml(String path) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream is = ResourceResolver.openStream(path)) {
            if (is == null) {
                log.warn("domain-api YAML resource not found at {}", path);
                return;
            }
            JsonNode root = mapper.readTree(is);
            if (root == null || !root.isObject()) return;
            Iterator<String> domains = root.fieldNames();
            while (domains.hasNext()) {
                String domain = domains.next();
                JsonNode apisNode = root.get(domain);
                if (apisNode == null || !apisNode.isObject()) continue;

                LinkedHashMap<String, ApiProperties> apiMap = new LinkedHashMap<>();
                Iterator<String> apis = apisNode.fieldNames();
                while (apis.hasNext()) {
                    String apiName = apis.next();
                    JsonNode apiNode = apisNode.get(apiName);
                    if (apiNode == null || !apiNode.isObject()) continue;
                    JsonNode url = apiNode.get("url");
                    if (url == null || url.isNull()) continue;
                    ApiProperties ap = new ApiProperties();
                    ap.setUrl(url.asText());
                    apiMap.put(apiName, ap);
                }
                this.domainApis.put(domain, apiMap);
            }
        }
    }

    public static class ApiProperties {

        public ApiProperties() {
        }

        /** API URL (may start with '{@domain}' or be a relative path) */
        private String url;

        /** Domain (HOST/logical) name */
        @Getter
        @ToString.Exclude
        private String domain;

        /** API logical name */
        @Getter
        @ToString.Exclude
        private String api;

        /** Cached parsed URL */
        @ToString.Exclude
        private String parsedUrl;

        public void setUrl(final String url) {
            this.url = url;
            this.parsedUrl = null;
        }

        public String getUrl() {
            if (StringUtils.isNotBlank(this.parsedUrl))
                return this.parsedUrl;

            // if already starts with '{@', return as is
            if (this.url != null && this.url.startsWith("{@")) {
                this.parsedUrl = this.url;
            } else {
                this.parsedUrl = String.format("{@%s}%s", this.domain, this.url == null ? "" : this.url);
            }
            return this.parsedUrl;
        }

        protected void setDomain(final String domain) {
            this.domain = domain;
        }

        protected void setApi(final String api) {
            this.api = api;
        }

        protected String findUrlVariableAndReplace(final String uriTemplateString) {
            return uriTemplateString.replace("{@" + this.domain + "." + this.api + "}", this.getUrl());
        }

        @Override
        public String toString() {
            return String.valueOf(this.url);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Reserved for future adjustments (e.g., circuit breaker config alignment)
    }
}
