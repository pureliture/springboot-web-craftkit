package com.teststrategy.multimodule.maven.sf.framework.rest.client.chain;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.OrderComparator;
import org.springframework.web.util.UriTemplateHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Binds a list of UriTemplateHandlerInterceptorChain beans into a single chain,
 * wiring their 'next' delegate in order and returning the head handler.
 */
public class UriTemplateHandlerInterceptorBinder {

    private final ObjectProvider<UriTemplateHandlerInterceptorChain> chainsProvider;
    private final UriTemplateHandlerInterceptorFinalizer finalizer;

    public UriTemplateHandlerInterceptorBinder(ObjectProvider<UriTemplateHandlerInterceptorChain> chainsProvider,
                                               UriTemplateHandlerInterceptorFinalizer finalizer) {
        this.chainsProvider = chainsProvider;
        this.finalizer = finalizer;
    }

    public UriTemplateHandler bind() {
        List<UriTemplateHandlerInterceptorChain> chains = new ArrayList<>();
        this.chainsProvider.forEach(chains::add);
        chains.sort(new OrderComparator());

        UriTemplateHandler tail = finalizer.finalHandler();
        for (int i = chains.size() - 1; i >= 0; i--) {
            UriTemplateHandlerInterceptorChain current = chains.get(i);
            current.setNext(tail);
            tail = current;
        }
        return tail; // head (or final handler if none)
    }
}
