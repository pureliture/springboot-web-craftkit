package com.teststrategy.multimodule.maven.sf.framework.application.constant;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class HttpRequestConstant {

    private HttpRequestConstant() {
        super();
    }
    /**
     *  constant of custom header keys
     */
    public static final String HTTP_HEADER_USER_ID = "USER-ID";
    public static final String HTTP_HEADER_PROGRAM_ID = "PROGRAM-ID";
    public static final String HTTP_HEADER_FORWARDED_SERVICE = "Forwarded-Service";

    @Deprecated
    public static final String HTTP_HEADER_X_FORWARDED_ATTRIBUTE = CommonConstant.FORWARDED_ATTRIBUTE;
    public static final String HTTP_HEADER_TRANSACTION_ID = "Global-Transaction-ID";
    public static final String HTTP_HEADER_LOGLEVEL_KEY = "LogLevel";

    /**
     * gateway signature header
     */
    public static final String HTTP_HEADER_APPLICATION_NAME = "APPLICATION-NAME";
    public static final String HTTP_HEADER_SIGNATURE = "Header-Authorization";

    /**
     * constant of standard http keys
     */
    public static final String HTTP_HEADER_FORWARDED_FOR = "Forwarded-For";
    public static final String HTTP_HEADER_PROXY_CLIENT_IP = "Proxy-Client-IP";
    public static final String HTTP_HEADER_WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    public static final String HTTP_HEADER_HTTP_CLIENT_IP = "HTTP_HEADER_HTTP_CLIENT_IP";
    public static final String HTTP_HEADER_HTTP_FORWARDED_FOR = "HTTP_FORWARDED_FOR";
    public static final String HTTP_HEADER_HTTP_PROXYUSER_IP = "ProxyUser-Ip";
    /* client pc w-gear에서 추출해준 값 */
    public static final String HTTP_HEADER_XFF = "Authenticated-IP";

    public static final String HTTP_HEADER_BFF_CONTEXT_PATH = "BFF-Context-Path";

    /**
     * constant values for initialization
     */
    public static final String HTTP_HEADER_DATE_TIMEFORMAT = "yyyyMMdd'T'HHmmssZ";
    public static final String LOCALHOST = "127.0.0.1";
    public static final String UNDEFINED_IP = LOCALHOST;
    public static final String UNDEFINED_SERVICE = "undefined";
    // fixed list :: could not add elements
    public static final List<String> UNDEFINED_FIXED_FORWARDED_SERVICE = List.of(UNDEFINED_SERVICE);

    @SuppressWarnings({"java:S3599", "java:S1171", "java:S2386"})
    public static final List<Pattern> COMMON_ENDPOINT_PATTERNS_LIST = new ArrayList<Pattern>() {
        private static final long serialVersionUID = 5581442182261972718L;
        {
            add(Pattern.compile("^(/[\\w-.]+)?/(public|actuator|webjars|swagger|v2/api-docs|h2(-console)?)"));
        }
    };

}
