package com.springboot.craftkit.framework.application.setting;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.springboot.craftkit.framework.application.ApplicationContextUtil;
import com.springboot.craftkit.framework.application.constant.HeaderConstant;
import com.springboot.craftkit.framework.application.constant.HttpHeaderConstant;
import com.springboot.craftkit.framework.filter.CommonLoggingFilter;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Data
@ToString
@NoArgsConstructor
@ConfigurationProperties(prefix = RequestLoggingProperties.PREFIX, ignoreUnknownFields = false)
public class RequestLoggingProperties implements InitializingBean {

    public static final String PREFIX = "craftkit.common-request-logging-filter";
    private static final List<String> DEFAULT_EXCLUDE_MASKING_HEADERS = Arrays.asList(
            HeaderConstant.HEADER_USER_ID, HeaderConstant.HEADER_PROGRAM_ID,
            HeaderConstant.HEADER_FORWARDED_SERVICE, HeaderConstant.HEADER_GLOBAL_TRANSACTION_ID,
            HeaderConstant.HEADER_LOGLEVEL_KEY, HeaderConstant.HEADER_APPLICATION_NAME,
            HttpHeaderConstant.HTTP_HEADER_FORWARDED_FOR, HttpHeaderConstant.HTTP_HEADER_PROXY_CLIENT_IP,
            HttpHeaderConstant.HTTP_HEADER_WL_PROXY_CLIENT_IP, HttpHeaderConstant.HTTP_HEADER_HTTP_CLIENT_IP,
            HttpHeaderConstant.HTTP_HEADER_HTTP_FORWARDED_FOR, HttpHeaderConstant.HTTP_HEADER_HTTP_PROXYUSER_IP,
            HttpHeaderConstant.HTTP_HEADER_XFF, HttpHeaderConstant.HTTP_HEADER_BFF_CONTEXT_PATH,
            HttpHeaders.ACCEPT, HttpHeaders.ACCEPT_ENCODING, HttpHeaders.ACCEPT_LANGUAGE,
            HttpHeaders.CONTENT_ENCODING, HttpHeaders.CONTENT_LENGTH, HttpHeaders.CONTENT_TYPE,
            HttpHeaders.HOST, HttpHeaders.ORIGIN, HttpHeaders.REFERER, HttpHeaders.USER_AGENT,
            "X-B3-Parentspanid", "X-B3-Spanid", "X-B3-Traceid", "X-Envoy-Attempt-Count",
            "X-Envoy-Expected-Rq-Timeout-Ms", "X-Envoy-Internal", "X-Forwarded-Proto");

    @SuppressWarnings("LoggerInitializedWithForeignClass")
    public static final ch.qos.logback.classic.Logger loggerCommonLoggingFilter =
            (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(CommonLoggingFilter.class);

    private boolean enabled = false;

    private String beforeMessagePrefix = "-->[";

    private String beforeMessageSuffix = "]";

    private boolean includeHeaders = true;

    private boolean includeQueryString = true;

    private boolean includePayload = false;

    private boolean includeClientInfo = true;

    private Integer maxPayloadLength = 50;

    private String afterMessagePrefix = "<--[";

    private String afterMessageSuffix = "]";

    private List<String> excludeMaskingHeaders = new ArrayList<>();

    @JsonProperty(access = Access.READ_ONLY)
    private List<Pattern> excludeUrlPatterns = new ArrayList<>();

    public void setEnabled(boolean enabled) {
        if (enabled && !loggerCommonLoggingFilter.isDebugEnabled()) {
            loggerCommonLoggingFilter.setLevel(Level.DEBUG);
        }
        if (!enabled && loggerCommonLoggingFilter.isDebugEnabled()) {
            loggerCommonLoggingFilter.setLevel(Level.INFO);
        }
        this.enabled = enabled;
    }

    @JsonProperty(access = Access.WRITE_ONLY)
    public void setExcludeUrlPattern(List<String> patternStringList) {
        if (CollectionUtils.isEmpty(patternStringList)) {
            return;
        }
        excludeUrlPatterns = patternStringList.stream().map(Pattern::compile).collect(Collectors.toList());
    }

    public Predicate<String> getHeaderPredicate() {
        List<String> headers =
                Stream.concat(this.excludeMaskingHeaders.stream(), DEFAULT_EXCLUDE_MASKING_HEADERS.stream())
                        .distinct()
                        .toList();
        return header -> headers.stream().anyMatch(str -> str.equalsIgnoreCase(header));
    }

    @Override
    public void afterPropertiesSet() {
        try {
            CommonLoggingFilter filter =
                    (CommonLoggingFilter) ApplicationContextUtil.getBean(CommonLoggingFilter.class);

            filter.setBeforeMessagePrefix(this.getBeforeMessagePrefix());
            filter.setBeforeMessageSuffix(this.getBeforeMessageSuffix());
            filter.setIncludeHeaders(this.isIncludeHeaders());
            filter.setIncludeQueryString(this.isIncludeQueryString());
            filter.setIncludePayload(this.isIncludePayload());
            filter.setIncludeClientInfo(this.isIncludeClientInfo());
            filter.setMaxPayloadLength(this.getMaxPayloadLength());
            filter.setAfterMessagePrefix(this.getAfterMessagePrefix());
            filter.setAfterMessageSuffix(this.getAfterMessageSuffix());
            filter.setHeaderPredicate(this.getHeaderPredicate());
        } catch (Exception e) {
            log.warn("CommonLoggingFilter refresh : {}", e.getMessage());
        }
    }

}
