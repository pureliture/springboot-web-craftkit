package com.teststrategy.multimodule.maven.sf.framework.scope;

public class RequestScopeUtil {

    private RequestScopeUtil() {
        super();
    }

    private static RequestScopeStore scopeStore = null;

    private static synchronized void defineScope() {
        if (scopeStore != null) {
            return;
        }
        scopeStore = new ScopeStore();
    }

    public static RequestScopeAttribute getAttribute() {
        if (scopeStore == null) {
            defineScope();
        }
        return scopeStore.getAttribute();
    }

    public static ScopeStore getScopeStore() {
        if (scopeStore == null) {
            defineScope();
        }

        if (scopeStore instanceof ScopeStore) {
            return (ScopeStore) scopeStore;
        }
        return null;
    }
}
