package com.springboot.craftkit.framework.rest.client.chain;

import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriTemplateHandler;

/**
 * Provides the final delegate of the chain (Default Spring Uri expansion).
 */
public class UriTemplateHandlerInterceptorFinalizer {

    public UriTemplateHandler finalHandler() {
        return new DefaultUriBuilderFactory();
    }
}
