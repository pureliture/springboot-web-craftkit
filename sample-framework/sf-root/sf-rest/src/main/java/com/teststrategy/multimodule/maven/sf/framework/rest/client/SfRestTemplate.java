package com.teststrategy.multimodule.maven.sf.framework.rest.client;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Minimal custom RestTemplate type for sf-rest. Pointcut targets RestTemplate+ so AOP applies here too.
 */
public class SfRestTemplate extends RestTemplate {
    public SfRestTemplate() {
        super();
    }
    public SfRestTemplate(ClientHttpRequestFactory requestFactory) {
        super(requestFactory);
    }
    public SfRestTemplate(List<HttpMessageConverter<?>> messageConverters) {
        super(messageConverters);
    }
}
