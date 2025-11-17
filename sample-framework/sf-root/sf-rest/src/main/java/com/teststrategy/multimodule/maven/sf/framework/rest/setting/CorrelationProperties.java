package com.teststrategy.multimodule.maven.sf.framework.rest.setting;

import com.teststrategy.multimodule.maven.sf.framework.application.constant.HeaderConstant;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = CorrelationProperties.PREFIX)
public class CorrelationProperties {
    public static final String PREFIX = "sf-rest.correlation";

    /** Ensure a correlation/transaction header is present on outbound requests. */
    private boolean enabled = true;

    /** Header name to use. Defaults to a neutral transaction ID header. */
    private String headerName = HeaderConstant.HEADER_GLOBAL_TRANSACTION_ID;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }
}
