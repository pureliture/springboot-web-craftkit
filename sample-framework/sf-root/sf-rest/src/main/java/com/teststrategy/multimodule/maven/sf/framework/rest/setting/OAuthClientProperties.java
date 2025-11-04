package com.teststrategy.multimodule.maven.sf.framework.rest.setting;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = OAuthClientProperties.PREFIX)
public class OAuthClientProperties {
    public static final String PREFIX = "sf-rest.oauth";

    /** Enable adding OAuth token to outbound requests. */
    private boolean enabled = false;

    /** Strategy to obtain a token. For now only 'static' is supported. */
    private String strategy = "static";

    /** A static bearer token for simple scenarios or tests. Do NOT use in production; provide via external secret store. */
    private String staticToken = "";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public String getStaticToken() {
        return staticToken;
    }

    public void setStaticToken(String staticToken) {
        this.staticToken = staticToken;
    }
}
