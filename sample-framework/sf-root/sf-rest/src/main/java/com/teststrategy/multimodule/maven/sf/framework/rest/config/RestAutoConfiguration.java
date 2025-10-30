package com.teststrategy.multimodule.maven.sf.framework.rest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teststrategy.multimodule.maven.sf.framework.application.setting.DomainApiProperties;
import com.teststrategy.multimodule.maven.sf.framework.application.setting.DomainProperties;
import com.teststrategy.multimodule.maven.sf.framework.rest.client.*;
import com.teststrategy.multimodule.maven.sf.framework.rest.setting.*;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Auto-configuration for REST client essentials.
 * - Binds {@link HttpClientProperties}, {@link OAuthClientProperties}, {@link ForwardHeadersProperties}, {@link CorrelationProperties}
 * - Provides a {@link RestTemplate} configured with timeouts
 * - Adds optional interceptors (token/header-forwarding/correlation) based on properties
 */
@AutoConfiguration
@EnableConfigurationProperties({HttpClientProperties.class, OAuthClientProperties.class, ForwardHeadersProperties.class, CorrelationProperties.class, HmacAuthProperties.class, ErrorHandlerProperties.class, HttpClientRetryProperties.class})
public class RestAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RestClientErrorHandler restClientErrorHandler() {
        return new RestClientErrorHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate(RestTemplateBuilder builder, HttpClientProperties httpProps, RestClientErrorHandler errorHandler) {
        Duration connectTimeout = httpProps.getConnectTimeout();
        Duration readTimeout = httpProps.getReadTimeout();
        return builder
                .setConnectTimeout(connectTimeout)
                .setReadTimeout(readTimeout)
                .errorHandler(errorHandler)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenClientHttpRequestInterceptor tokenClientHttpRequestInterceptor(OAuthClientProperties oAuthClientProperties) {
        return new TokenClientHttpRequestInterceptor(oAuthClientProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ForwardHeadersClientHttpRequestInterceptor forwardHeadersClientHttpRequestInterceptor(ForwardHeadersProperties properties) {
        return new ForwardHeadersClientHttpRequestInterceptor(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public CorrelationIdClientHttpRequestInterceptor correlationIdClientHttpRequestInterceptor(CorrelationProperties properties) {
        return new CorrelationIdClientHttpRequestInterceptor(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public HmacClientHttpRequestInterceptor hmacClientHttpRequestInterceptor(HmacAuthProperties properties) {
        return new HmacClientHttpRequestInterceptor(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public BusinessErrorDetectingInterceptor businessErrorDetectingInterceptor(ErrorHandlerProperties errorHandlerProperties,
                                                                               ObjectProvider<ObjectMapper> objectMapperProvider) {
        return new BusinessErrorDetectingInterceptor(errorHandlerProperties, objectMapperProvider.getIfAvailable());
    }

    @Bean
    public RestTemplateCustomizer restTemplateCustomizer(TokenClientHttpRequestInterceptor tokenInterceptor,
                                                         OAuthClientProperties oAuthClientProperties,
                                                         ForwardHeadersClientHttpRequestInterceptor forwardHeadersInterceptor,
                                                         ForwardHeadersProperties forwardHeadersProperties,
                                                         CorrelationIdClientHttpRequestInterceptor correlationInterceptor,
                                                         CorrelationProperties correlationProperties,
                                                         HmacClientHttpRequestInterceptor hmacInterceptor,
                                                         HmacAuthProperties hmacAuthProperties,
                                                         BusinessErrorDetectingInterceptor businessErrorDetectingInterceptor,
                                                         ErrorHandlerProperties errorHandlerProperties) {
        return restTemplate -> {
            if (correlationProperties.isEnabled()) {
                restTemplate.getInterceptors().add(correlationInterceptor);
            }
            if (forwardHeadersProperties.isEnabled()) {
                restTemplate.getInterceptors().add(forwardHeadersInterceptor);
            }
            if (oAuthClientProperties.isEnabled()) {
                restTemplate.getInterceptors().add(tokenInterceptor);
            }
            if (hmacAuthProperties.isEnabled()) {
                restTemplate.getInterceptors().add(hmacInterceptor);
            }
            if (errorHandlerProperties.isEnabled()) {
                restTemplate.getInterceptors().add(businessErrorDetectingInterceptor);
            }
        };
    }

    // --- HttpClient5-based retry wiring (opt-in) ---

    @Bean
    @ConditionalOnClass(CloseableHttpClient.class)
    @ConditionalOnProperty(prefix = HttpClientRetryProperties.PREFIX, name = "enabled", havingValue = "true")
    public HttpRequestRetryStrategy httpRequestRetryStrategy(HttpClientRetryProperties props) {
        return new HttpClient5RetryStrategy(props);
    }

    @Bean
    @ConditionalOnClass(CloseableHttpClient.class)
    @ConditionalOnProperty(prefix = HttpClientRetryProperties.PREFIX, name = "enabled", havingValue = "true")
    public CloseableHttpClient httpClientWithRetry(HttpClientProperties httpProps,
                                                   HttpRequestRetryStrategy retryStrategy) {
        PoolingHttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(httpProps.getMaxConnTotal())
                .setMaxConnPerRoute(httpProps.getMaxConnPerRoute())
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(httpProps.getConnectTimeout().toMillis()))
                .setResponseTimeout(Timeout.ofMilliseconds(httpProps.getReadTimeout().toMillis()))
                .build();

        return HttpClients.custom()
                .setConnectionManager(cm)
                .setRetryStrategy(retryStrategy)
                .setDefaultRequestConfig(requestConfig)
                .evictExpiredConnections()
                .build();
    }

    @Bean
    @ConditionalOnClass(CloseableHttpClient.class)
    @ConditionalOnProperty(prefix = HttpClientRetryProperties.PREFIX, name = "enabled", havingValue = "true")
    public RestTemplateCustomizer httpClient5RetryRestTemplateCustomizer(CloseableHttpClient httpClient) {
        return restTemplate -> restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
    }

    @Bean
    @ConditionalOnBean(DomainProperties.class)
    public RestTemplateCustomizer domainUriTemplateHandlerCustomizer(DomainProperties domainProperties,
                                                                     ObjectProvider<DomainApiProperties> domainApiProperties,
                                                                     Environment environment) {
        return restTemplate -> restTemplate.setUriTemplateHandler(
                new com.teststrategy.multimodule.maven.sf.framework.rest.client.DomainUriTemplateHandler(
                        domainProperties,
                        domainApiProperties.getIfAvailable(),
                        environment
                )
        );
    }
}
