package com.springboot.craftkit.framework.rest.setting;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = ForwardHeadersProperties.PREFIX)
public class ForwardHeadersProperties {
    public static final String PREFIX = "sf-rest.forward-headers";

    /** Enable forwarding some inbound headers to outbound requests. */
    private boolean enabled = true;

    /** Header names to forward if present in the inbound request. */
    private List<String> names = new ArrayList<>(Arrays.asList(
            "Authorization",
            "USER-ID",
            "PROGRAM-ID",
            "Forwarded-Service",
            "Global-Transaction-ID"
    ));

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }
}
