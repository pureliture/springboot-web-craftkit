package com.teststrategy.multimodule.maven.sf.framework.rest.setting;

import com.teststrategy.multimodule.maven.sf.framework.application.constant.HttpRequestConstant;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = HmacAuthProperties.PREFIX)
public class HmacAuthProperties {
    public static final String PREFIX = "sample-framework.rest.hmac";

    /** Enable adding an HMAC signature header to outbound requests. */
    private boolean enabled = false;

    /** Identifier of the key (public part). */
    private String keyId;

    /** Shared secret. Must be provided via an external secret store or environment, never hard-coded. */
    private String secret;

    /** Header name to carry the signature. */
    private String headerName = HttpRequestConstant.HTTP_HEADER_SIGNATURE;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }
}
