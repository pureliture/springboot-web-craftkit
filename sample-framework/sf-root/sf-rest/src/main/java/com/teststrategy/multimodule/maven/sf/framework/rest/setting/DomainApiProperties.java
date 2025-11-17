package com.teststrategy.multimodule.maven.sf.framework.rest.setting;

import com.teststrategy.multimodule.maven.sf.framework.rest.setting.exception.SfDomainConfigException;
import com.teststrategy.multimodule.maven.sf.framework.rest.setting.loader.DomainApiLoader;
import com.teststrategy.multimodule.maven.sf.framework.rest.setting.loader.SpringBindableDomainApiLoader;
import com.teststrategy.multimodule.maven.sf.framework.rest.setting.loader.YamlObjectMapperDomainApiLoader;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.util.UriTemplate;

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
 */
@Slf4j
public class DomainApiProperties implements InitializingBean {

    public static final String BULK_REQUEST_METHOD = ":bulkProcess";

    public static final String CONFIG_PATH = "sf-rest.domain.api.config"; // primary key
    public static final String LOADER_KEY = "sf-rest.domain.api.loader"; // simple|bind
    public static final String PLACEHOLDERS = "${" + CONFIG_PATH + ":}";

    @ToString.Exclude
    private final transient Environment environment;

    @ToString.Exclude
    private final transient ResourceLoader resourceLoader;

    @Getter
    private final String configPath;

    /** Selected loader id for logging */
    private final String loaderId;

    /**
     * domainName -> (apiName -> ApiProperties)
     */
    LinkedHashMap<String, LinkedHashMap<String, ApiProperties>> domainApis = new LinkedHashMap<>();

    public DomainApiProperties(final Environment environment) {
        this(environment, new org.springframework.core.io.DefaultResourceLoader());
    }

    public DomainApiProperties(final Environment environment, final ResourceLoader resourceLoader) {
        this.environment = environment;
        this.resourceLoader = resourceLoader != null ? resourceLoader : new org.springframework.core.io.DefaultResourceLoader();

        // choose loader
        String loaderProp = environment != null ? environment.getProperty(LOADER_KEY, "simple") : "simple";
        DomainApiLoader loader = ("bind".equalsIgnoreCase(loaderProp)) ? new SpringBindableDomainApiLoader() : new YamlObjectMapperDomainApiLoader();
        this.loaderId = loader.id();

        // resolve configPath with precedence: sf-rest key first
        String sfResolved = environment != null ? environment.resolvePlaceholders("${" + CONFIG_PATH + ":}") : null;
        if (!isUnset(sfResolved)) {
            this.configPath = sfResolved;
        } else {
            String msg = "Configuration path not set. Please configure either '" + CONFIG_PATH + "'.";
            log.warn(msg);
            throw new SfDomainConfigException(msg);
        }

        try {
            LinkedHashMap<String, LinkedHashMap<String, ApiProperties>> loaded = loader.load(this.resourceLoader, this.configPath);
            this.domainApis = loaded != null ? loaded : new LinkedHashMap<>();
            // backfill domain/api names into each ApiProperties
            if (domainApis != null) {
                domainApis.forEach((domain, apis) -> {
                    if (apis != null) {
                        apis.forEach((apiName, api) -> {
                            if (api != null) {
                                api.setDomain(domain);
                                api.setApi(apiName);
                            }
                        });
                    }
                });
            }
            int domainCount = this.domainApis.size();
            int apiCount = this.domainApis.values().stream().mapToInt(m -> m != null ? m.size() : 0).sum();
            log.info("DomainApiProperties initialized. loader={}, configPath={}, domains={}, apis={}", this.loaderId, this.configPath, domainCount, apiCount);
        } catch (Exception e) {
            String msg = "Failed to load domain-api from '" + this.configPath + "' using loader '" + this.loaderId + "': " + e.getMessage();
            log.warn(msg, e);
            throw new SfDomainConfigException(msg, e);
        }
    }

    /**
     * @deprecated Use getOptionalApi(uri) instead.
     */
    @Deprecated
    public Optional<ApiProperties> getApi(final String uriTemplateString) {
        return getOptionalApi(uriTemplateString);
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

    private static boolean isUnset(String value) {
        return StringUtils.isBlank(value) || "null".equalsIgnoreCase(value.trim());
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

        /** Enable RestClient error handler (200-with-error detection) for this API. */
        @Getter @Setter
        private Boolean enabledRestClientErrorHandler;

        /** Per-API circuit breaker property holder (stored only). */
        @Getter @Setter
        private SfRestCircuitBreakerProperties circuitBreaker = new SfRestCircuitBreakerProperties();

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
        if (this.domainApis == null || this.domainApis.isEmpty()) {
            log.warn("DomainApiProperties loaded but contains no entries. configPath={} loader={}", this.configPath, this.loaderId);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DomainApiProperties (")
                .append(this.configPath).append(") [");
        if (this.domainApis != null) {
            this.domainApis.forEach((d, group) -> {
                if (group != null) {
                    group.forEach((a, api) -> sb.append('\n').append('\t').append(d).append('.').append(a).append(" = ").append(api));
                }
            });
        }
        return sb.append('\n').append(']').toString();
    }
}
