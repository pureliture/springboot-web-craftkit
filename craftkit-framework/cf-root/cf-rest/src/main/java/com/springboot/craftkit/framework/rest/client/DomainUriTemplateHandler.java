package com.springboot.craftkit.framework.rest.client;

import com.springboot.craftkit.framework.rest.client.chain.UriTemplateHandlerInterceptorChain;
import com.springboot.craftkit.framework.rest.setting.DomainApiProperties;
import com.springboot.craftkit.framework.rest.setting.DomainProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriTemplateHandler;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UriTemplateHandler that resolves '{@domain.api}' and '{@domain}' placeholders automatically
 * using {@link DomainApiProperties} and {@link DomainProperties} before delegating to the
 * next handler in the chain (default Spring expansion by default).
 */
public class DomainUriTemplateHandler extends UriTemplateHandlerInterceptorChain {

    private static final Pattern DOMAIN_MATCH_PATTERN = Pattern.compile("(?<=^\\{@)[a-zA-Z0-9-_]+(?=\\})");

    @Nullable
    private final DomainProperties domains;
    @Nullable
    private final DomainApiProperties domainApis;
    @Nullable
    private final Environment environment;

    public DomainUriTemplateHandler(@Nullable DomainProperties domains,
                                    @Nullable DomainApiProperties domainApis,
                                    @Nullable Environment environment) {
        this(domains, domainApis, environment, new DefaultUriBuilderFactory());
    }

    public DomainUriTemplateHandler(@Nullable DomainProperties domains,
                                    @Nullable DomainApiProperties domainApis,
                                    @Nullable Environment environment,
                                    @NonNull UriTemplateHandler delegate) {
        this.domains = domains;
        this.domainApis = domainApis;
        this.environment = environment;
        this.next = delegate;
    }

    @NonNull
    @Override
    public URI expand(@NonNull String uriTemplate, @NonNull Map<String, ?> uriVariables) {
        String prepared = prepareUriTemplate(uriTemplate);
        return next.expand(prepared, uriVariables);
    }

    @NonNull
    @Override
    public URI expand(@NonNull String uriTemplate, @NonNull Object... uriVariables) {
        String prepared = prepareUriTemplate(uriTemplate);
        return next.expand(prepared, uriVariables);
    }

    private String prepareUriTemplate(String uriTemplateString) {
        final String original = uriTemplateString;
        // Reset context to avoid leaking previous domain.api across calls
        DomainApiContext.clear();

        // If there is no domain info configured at all, return as-is (no auto mapping).
        if (this.domains == null) {
            // still resolve environment placeholders best-effort
            return resolveEnvPlaceholders(uriTemplateString);
        }

        // Step 1: capture '{@domain.api}' id for downstream (e.g., circuit breaker naming), then resolve via domain-api.yml
        if (this.domainApis != null) {
            Matcher m = Pattern.compile("\\{@([\\\\w-]+\\\\.[\\\\w-]+)\\}").matcher(uriTemplateString);
            if (m.find()) {
                String id = m.group(1); // domain.api
                DomainApiContext.setCurrentDomainApi(id);
            }
            uriTemplateString = this.domainApis.getUri(uriTemplateString);
        }

        // Step 2: resolve leading '{@domain}' from domain.yml
        final Matcher domainMatcher = DOMAIN_MATCH_PATTERN.matcher(uriTemplateString);
        if (domainMatcher.find()) {
            final String targetDomain = domainMatcher.group();
            if (this.domains.hasDomain(targetDomain)) {
                final String uriWithoutDomain = uriTemplateString.replaceFirst("^\\{@" + Pattern.quote(targetDomain) + "\\}", "");
                final boolean isBulkRequest = isBulkRequest(uriWithoutDomain);
                final String baseUrl = this.domains.getDomainUrl(targetDomain, isBulkRequest);
                if (StringUtils.isBlank(baseUrl)) {
                    throw new DomainUriMappingException("Domain URL is not configured for '" + targetDomain + "' (bulk=" + isBulkRequest + ")");
                }
                String joined = baseUrl.concat(uriWithoutDomain);
                joined = resolveEnvPlaceholders(joined);
                return joined;
            }
        }

        // If unresolved '{@' remains, throw explicit error to guide configuration
        if (uriTemplateString.contains("{@")) {
            String domainsPath = Optional.ofNullable(this.domains.getConfigPath()).orElse("sf-rest.domain.config not set");
            String apisPath = Optional.ofNullable(this.domainApis).map(DomainApiProperties::getConfigPath)
                    .orElse("sf-rest.domain.api.config not set");
            throw new DomainUriMappingException(
                    "URI mapping failed. Check domain config (" + domainsPath + ") and API config (" + apisPath + ") for input URL: " + original);
        }

        // Just resolve environment placeholders and return
        return resolveEnvPlaceholders(uriTemplateString);
    }

    private boolean isBulkRequest(String uriWithoutDomain) {
        return StringUtils.contains(uriWithoutDomain, DomainApiProperties.BULK_REQUEST_METHOD);
    }

    private String resolveEnvPlaceholders(String value) {
        if (this.environment != null && value != null && value.contains("${")) {
            return this.environment.resolvePlaceholders(value);
        }
        return value;
    }
}
