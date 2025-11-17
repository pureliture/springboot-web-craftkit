package com.springboot.craftkit.framework.rest.client;

import org.springframework.boot.web.client.RestTemplateBuilder;

/**
 * Minimal builder wrapper for {@link SfRestTemplate} built on top of Spring's {@link RestTemplateBuilder}.
 */
public class SfRestTemplateBuilder {
    private final RestTemplateBuilder delegate;

    public SfRestTemplateBuilder(RestTemplateBuilder delegate) {
        this.delegate = delegate;
    }

    /**
     * Build an {@link SfRestTemplate} using the current delegate settings.
     */
    public SfRestTemplate build() {
        return delegate.build(SfRestTemplate.class);
    }

    /**
     * Access to the underlying {@link RestTemplateBuilder} for advanced customization.
     */
    public RestTemplateBuilder delegate() {
        return delegate;
    }
}
