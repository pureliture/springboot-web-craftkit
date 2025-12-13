package com.springboot.craftkit.framework.application.setting;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ToString
@NoArgsConstructor
@ConfigurationProperties(prefix = UrlRewriteFilterProperties.PREFIX, ignoreUnknownFields = false)
public class UrlRewriteFilterProperties {

    public static final String PREFIX = "craftkit.url-rewrite-filter";

    private boolean enabled;
    private String targetUri;
    private String httpsPort;
    private String httpPort;
}
