package com.springboot.craftkit.framework.rest.setting.exception;

/**
 * Thrown when DomainApiProperties cannot determine a configuration path from either supported keys.
 */
public class SfDomainConfigException extends RuntimeException {
    public SfDomainConfigException(String message) {
        super(message);
    }
    public SfDomainConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
