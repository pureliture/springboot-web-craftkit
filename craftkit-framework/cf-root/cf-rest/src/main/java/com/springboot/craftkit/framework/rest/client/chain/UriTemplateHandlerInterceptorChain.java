package com.springboot.craftkit.framework.rest.client.chain;

import org.springframework.core.Ordered;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriTemplateHandler;

/**
 * Simple chain-of-responsibility base for composing UriTemplateHandler processing
 */
public abstract class UriTemplateHandlerInterceptorChain implements UriTemplateHandler, Ordered {

    protected UriTemplateHandler next = new DefaultUriBuilderFactory();

    public void setNext(UriTemplateHandler next) {
        this.next = next;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
