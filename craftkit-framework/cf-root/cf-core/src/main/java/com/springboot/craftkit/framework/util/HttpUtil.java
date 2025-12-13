package com.springboot.craftkit.framework.util;

import com.springboot.craftkit.framework.application.constant.HttpHeaderConstant;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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

        String ip = extractClientIpByForwardedFor(request.getHeader(HttpHeaderConstant.HTTP_HEADER_XFF));

        if (isUnknownIp(ip)) {
            ip = extractClientIpByForwardedFor(request.getHeader(HttpHeaderConstant.HTTP_HEADER_FORWARDED_FOR));
        }
        if (isUnknownIp(ip)) {
            ip = request.getHeader(HttpHeaderConstant.HTTP_HEADER_PROXY_CLIENT_IP);
        }
        if (isUnknownIp(ip)) {
            ip = request.getHeader(HttpHeaderConstant.HTTP_HEADER_WL_PROXY_CLIENT_IP);
        }
        if (isUnknownIp(ip)) {
            ip = request.getHeader(HttpHeaderConstant.HTTP_HEADER_HTTP_CLIENT_IP);
        }
        if (isUnknownIp(ip)) {
            ip = request.getHeader(HttpHeaderConstant.HTTP_HEADER_HTTP_FORWARDED_FOR);
        }
        if (isUnknownIp(ip)) {
            // An alternative form of the header (X-ProxyUser-Ip) is used by Google clients talking to Google servers.
            ip = request.getHeader(HttpHeaderConstant.HTTP_HEADER_HTTP_PROXYUSER_IP);
        }
        if (isUnknownIp(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private static boolean isUnknownIp(String ip) {
        return StringUtils.isBlank(ip)
                || "unknown".equalsIgnoreCase(ip)
                || HttpHeaderConstant.UNDEFINED_IP.equalsIgnoreCase(ip);
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

    /**
     * Check if the path matches common endpoint patterns (swagger, actuator, webjars, etc.)
     * <pre>
     * "^(/\\w+)?/actuator/"
     * "^(/\\w+)?/webjars/"
     * "^(/\\w+)?/swagger"
     * "^(/\\w+)?/v2/api-docs"
     * </pre>
     *
     * @param path the request path
     * @return true if the path matches any common endpoint pattern
     */
    public static boolean isCommonEndpointPatternContains(String path) {
        return isPatternContains(path, HttpHeaderConstant.COMMON_ENDPOINT_PATTERNS_LIST);
    }

    /**
     * Check if the given string matches any pattern in the pattern list.
     *
     * @param path the string to check
     * @param patternList the list of patterns to match against
     * @return true if the path matches any pattern
     */
    public static boolean isPatternContains(String path, List<Pattern> patternList) {
        return patternList.stream()
                .map(pattern -> pattern.matcher(path))
                .anyMatch(Matcher::find);
    }
}
