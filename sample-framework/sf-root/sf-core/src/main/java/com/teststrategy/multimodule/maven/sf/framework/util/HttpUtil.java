package com.teststrategy.multimodule.maven.sf.framework.util;

import com.teststrategy.multimodule.maven.sf.framework.application.constant.HttpRequestConstant;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;


@Slf4j
public class HttpUtil {

    private HttpUtil() {
        super();
    }

    public static String generateNewGtid() {
        return new StringBuilder()
                .append(PropertyUtil.getApplicationName())
                .append('-')
                .append(Long.toUnsignedString(Instant.now().toEpochMilli(), 36))
                .append(RandomStringUtils.randomAlphanumeric(9))
                .toString();
    }

    /**
     * 수행 중인 요청의 HttpServletRequest 의 헤더 값을 가져온다.
     *
     * <pre>
     *     주의사항)  Async, hystrix thread, 비동기 메세지 처리(kafka) 등의 작업은 값을 가져올수 없다.
     * </pre>
     */
    public static String getCurrentRequestHeaderString(String headerName) {
        HttpServletRequest request = getHttpRequest();
        if (request == null)
            return "";

        return StringUtils.defaultString(request.getHeader(headerName));
    }

    public static String getClientIp(HttpServletRequest request) {

        String ip = extractClientIpByForwardedFor(request.getHeader(HttpRequestConstant.HTTP_HEADER_XFF));

        if (isUnknownIp(ip)) {
            ip = extractClientIpByForwardedFor(request.getHeader(HttpRequestConstant.HTTP_HEADER_FORWARDED_FOR));
        }
        if (isUnknownIp(ip)) {
            ip = request.getHeader(HttpRequestConstant.HTTP_HEADER_PROXY_CLIENT_IP);
        }
        if (isUnknownIp(ip)) {
            ip = request.getHeader(HttpRequestConstant.HTTP_HEADER_WL_PROXY_CLIENT_IP);
        }
        if (isUnknownIp(ip)) {
            ip = request.getHeader(HttpRequestConstant.HTTP_HEADER_HTTP_CLIENT_IP);
        }
        if (isUnknownIp(ip)) {
            ip = request.getHeader(HttpRequestConstant.HTTP_HEADER_HTTP_FORWARDED_FOR);
        }
        if (isUnknownIp(ip)) {
            // An alternative form of the header (X-ProxyUser-Ip) is used by Google clients talking to Google servers.
            ip = request.getHeader(HttpRequestConstant.HTTP_HEADER_HTTP_PROXYUSER_IP);
        }
        if (isUnknownIp(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private static boolean isUnknownIp(String ip) {
        return StringUtils.isBlank(ip)
                || "unknown".equalsIgnoreCase(ip)
                || HttpRequestConstant.UNDEFINED_IP.equalsIgnoreCase(ip);
    }

    private static String extractClientIpByForwardedFor(String headerValue) {
        if (StringUtils.isBlank(headerValue))
            return null;

        try {
            return headerValue.replaceAll("\\s*,.*$", "");
        } catch (Exception e) {
            log.warn("extractClientIpByForwardedFor error {}", e.getMessage());
        }
        return null;
    }

    public static HttpServletRequest getHttpRequest() {
        try {
            ServletRequestAttributes servletRequestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return servletRequestAttributes.getRequest();
        } catch (Exception e) {
            return null;
        }
    }
}
