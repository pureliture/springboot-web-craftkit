package com.teststrategy.multimodule.maven.sf.framework.error;

/**
 * Holds the per-request error-detail exposure decision.
 */
public final class ErrorDetailContext {

    public static final String REQ_ATTR_ENABLED = "sample.error.detail.enabled";

    private static final ThreadLocal<Boolean> HOLDER = new ThreadLocal<>();

    private ErrorDetailContext() {}

    public static void setEnabled(Boolean enabled) {
        HOLDER.set(enabled);
    }

    public static boolean isEnabled() {
        Boolean v = HOLDER.get();
        return v != null && v;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
