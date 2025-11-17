package com.springboot.craftkit.framework.application.constant;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * HTTP 전용 헤더 상수와 보조 값을 모아둔 클래스입니다.
 * HTTP 전송에만 사용하며, 필요 시 {@link HeaderConstant}와 함께 사용하세요.
 */
public final class HttpHeaderConstant {

    private HttpHeaderConstant() {
        throw new IllegalStateException("Utility class");
    }

    // 이 프로젝트에서 사용하는 표준(혹은 관례적) HTTP 헤더 키
    public static final String HTTP_HEADER_FORWARDED_FOR = "Forwarded-For";
    public static final String HTTP_HEADER_PROXY_CLIENT_IP = "Proxy-Client-IP";
    public static final String HTTP_HEADER_WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    public static final String HTTP_HEADER_HTTP_CLIENT_IP = "HTTP_HEADER_HTTP_CLIENT_IP";
    public static final String HTTP_HEADER_HTTP_FORWARDED_FOR = "HTTP_FORWARDED_FOR";
    public static final String HTTP_HEADER_HTTP_PROXYUSER_IP = "ProxyUser-Ip";
    /* 클라이언트 PC W-GEAR에서 추출해 준 값 */
    public static final String HTTP_HEADER_XFF = "Authenticated-IP";

    public static final String HTTP_HEADER_BFF_CONTEXT_PATH = "BFF-Context-Path";

    // HTTP 전용 초기값/공통 값
    public static final String LOCALHOST = "127.0.0.1";
    public static final String UNDEFINED_IP = LOCALHOST;

    @SuppressWarnings({"java:S3599", "java:S1171", "java:S2386"})
    public static final List<Pattern> COMMON_ENDPOINT_PATTERNS_LIST = new ArrayList<>() {
        @Serial
        private static final long serialVersionUID = 5581442182261972718L;
        {
            // 공용/헬스체크/문서/콘솔 등의 엔드포인트 패턴
            add(Pattern.compile("^(/[\\w-.]+)?/(public|actuator|webjars|swagger|v2/api-docs|h2(-console)?)"));
        }
    };
}