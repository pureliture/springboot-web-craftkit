package com.teststrategy.multimodule.maven.sf.framework.application.constant;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * HTTP-specific header constants and supporting values.
 * Use these only for HTTP transport; combine with {@link HeaderConstant} as needed.
 */
public final class HttpHeaderConstant {

    private HttpHeaderConstant() {
        throw new IllegalStateException("Utility class");
    }

    // Standard HTTP header keys (as used in this project)
    public static final String HTTP_HEADER_FORWARDED_FOR = "Forwarded-For";
    public static final String HTTP_HEADER_PROXY_CLIENT_IP = "Proxy-Client-IP";
    public static final String HTTP_HEADER_WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    public static final String HTTP_HEADER_HTTP_CLIENT_IP = "HTTP_HEADER_HTTP_CLIENT_IP";
    public static final String HTTP_HEADER_HTTP_FORWARDED_FOR = "HTTP_FORWARDED_FOR";
    public static final String HTTP_HEADER_HTTP_PROXYUSER_IP = "ProxyUser-Ip";
    /* client pc w-gear에서 추출해준 값 */
    public static final String HTTP_HEADER_XFF = "Authenticated-IP";

    public static final String HTTP_HEADER_BFF_CONTEXT_PATH = "BFF-Context-Path";

    // HTTP-only initialization/common values
    public static final String LOCALHOST = "127.0.0.1";
    public static final String UNDEFINED_IP = LOCALHOST;

    @SuppressWarnings({"java:S3599", "java:S1171", "java:S2386"})
    public static final List<Pattern> COMMON_ENDPOINT_PATTERNS_LIST = new ArrayList<Pattern>() {
        private static final long serialVersionUID = 5581442182261972718L;
        {
            add(Pattern.compile("^(/[\\w-.]+)?/(public|actuator|webjars|swagger|v2/api-docs|h2(-console)?)"));
        }
    };
}
