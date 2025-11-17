package com.springboot.craftkit.framework.rest.client;

/**
 * Simple holder for the current '{@domain.api}' identifier extracted from the
 * URI template before expansion. Used to derive circuit breaker instance names.
 */
public final class DomainApiContext {
    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private DomainApiContext() {}

    public static void setCurrentDomainApi(String id) {
        CURRENT.set(id);
    }

    public static String getCurrentDomainApi() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
