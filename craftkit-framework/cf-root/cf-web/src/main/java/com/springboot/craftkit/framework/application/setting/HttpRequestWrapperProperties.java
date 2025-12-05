package com.springboot.craftkit.framework.application.setting;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = HttpRequestWrapperProperties.PREFIX, ignoreUnknownFields = false)
public class HttpRequestWrapperProperties {

    public static final String PREFIX = "craftkit.http-request-wrapper-filter";

    private boolean enabled;
    private String urlPatterns;
}
