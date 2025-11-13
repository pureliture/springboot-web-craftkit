package com.teststrategy.multimodule.maven.sf.framework.rest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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
@EnableConfigurationProperties({HttpClientProperties.class, OAuthClientProperties.class, ForwardHeadersProperties.class, CorrelationProperties.class, HmacAuthProperties.class, ErrorHandlerProperties.class, HttpClientRetryProperties.class, HttpClientEvictorProperties.class})
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
    public PoolingHttpClientConnectionManager httpClientConnectionManager(HttpClientProperties httpProps) {
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(httpProps.getMaxConnTotal())
                .setMaxConnPerRoute(httpProps.getMaxConnPerRoute())
                .build();
    }

    @Bean
    @ConditionalOnClass(CloseableHttpClient.class)
    @ConditionalOnProperty(prefix = HttpClientRetryProperties.PREFIX, name = "enabled", havingValue = "true")
    public CloseableHttpClient httpClientWithRetry(HttpClientProperties httpProps,
                                                   HttpRequestRetryStrategy retryStrategy,
                                                   PoolingHttpClientConnectionManager connectionManager) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(httpProps.getConnectTimeout().toMillis()))
                .setResponseTimeout(Timeout.ofMilliseconds(httpProps.getReadTimeout().toMillis()))
                .build();

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
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
    @ConditionalOnMissingBean
    public com.teststrategy.multimodule.maven.sf.framework.rest.client.DomainUriTemplateHandler domainUriTemplateHandler(
            DomainProperties domainProperties,
            ObjectProvider<DomainApiProperties> domainApiProperties,
            Environment environment) {
        return new com.teststrategy.multimodule.maven.sf.framework.rest.client.DomainUriTemplateHandler(
                domainProperties,
                domainApiProperties.getIfAvailable(),
                environment
        );
    }

    @Bean
    @ConditionalOnBean(DomainProperties.class)
    public com.teststrategy.multimodule.maven.sf.framework.rest.client.chain.UriTemplateHandlerInterceptorFinalizer uriTemplateHandlerInterceptorFinalizer() {
        return new com.teststrategy.multimodule.maven.sf.framework.rest.client.chain.UriTemplateHandlerInterceptorFinalizer();
    }

    @Bean
    @ConditionalOnBean(DomainProperties.class)
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
    public com.teststrategy.multimodule.maven.sf.framework.rest.client.DtoUriTemplateHandler dtoUriTemplateHandler() {
        return new com.teststrategy.multimodule.maven.sf.framework.rest.client.DtoUriTemplateHandler();
    }

    @Bean
    @ConditionalOnBean(DomainProperties.class)
    public com.teststrategy.multimodule.maven.sf.framework.rest.client.chain.UriTemplateHandlerInterceptorBinder uriTemplateHandlerInterceptorBinder(
            org.springframework.beans.factory.ObjectProvider<com.teststrategy.multimodule.maven.sf.framework.rest.client.chain.UriTemplateHandlerInterceptorChain> chainsProvider,
            com.teststrategy.multimodule.maven.sf.framework.rest.client.chain.UriTemplateHandlerInterceptorFinalizer finalizer) {
        return new com.teststrategy.multimodule.maven.sf.framework.rest.client.chain.UriTemplateHandlerInterceptorBinder(chainsProvider, finalizer);
    }

    @Bean
    @ConditionalOnBean(DomainProperties.class)
    public RestTemplateCustomizer uriTemplateHandlerChainCustomizer(
            com.teststrategy.multimodule.maven.sf.framework.rest.client.chain.UriTemplateHandlerInterceptorBinder binder) {
        return restTemplate -> restTemplate.setUriTemplateHandler(binder.bind());
    }

    @Bean
    @ConditionalOnClass(org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager.class)
    @ConditionalOnBean(org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager.class)
    @ConditionalOnProperty(prefix = com.teststrategy.multimodule.maven.sf.framework.rest.setting.HttpClientEvictorProperties.PREFIX, name = "enabled", havingValue = "true")
    public com.teststrategy.multimodule.maven.sf.framework.rest.client.IdleConnectionEvictor idleConnectionEvictor(
            org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager connectionManager,
            com.teststrategy.multimodule.maven.sf.framework.rest.setting.HttpClientEvictorProperties evictorProperties) {
        return new com.teststrategy.multimodule.maven.sf.framework.rest.client.IdleConnectionEvictor(
                connectionManager,
                evictorProperties.getIdleTimeMillis(),
                evictorProperties.getCheckIntervalMillis()
        );
    }
}
