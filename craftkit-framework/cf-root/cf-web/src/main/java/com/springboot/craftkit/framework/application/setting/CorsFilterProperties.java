package com.springboot.craftkit.framework.application.setting;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ToString
@NoArgsConstructor
@ConfigurationProperties(prefix = CorsFilterProperties.PREFIX, ignoreUnknownFields = false)
public class CorsFilterProperties {

    public static final String PREFIX = "craftkit.cors-filter";

    private boolean enabled;
    private String urlPatterns;
    private String accessControlAllowOrigin = "Content-Type";
    private boolean accessControlAllowCredentials = false;
    private String accessControlAllowHeaders = "Content-Type";
    private String accessControlAllowMethods = "GET, POST, PUT, DELETE";
    private int accessControlMaxAge = 1;
}
