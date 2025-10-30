package com.teststrategy.multimodule.maven.sf.framework.rest.client;

/**
 * Thrown when '{@domain}' or '{@domain.api}' placeholders cannot be resolved due to
 * missing configuration in domain.yml or domain-api.yml.
 */
public class DomainUriMappingException extends RuntimeException {
    public DomainUriMappingException(String message) {
        super(message);
    }
}
