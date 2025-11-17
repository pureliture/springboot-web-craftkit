package com.springboot.craftkit.framework.scope;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.springboot.craftkit.framework.util.PropertyUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@JsonFilter("scopeAttributeJsonFilter")
public class ScopeAttribute implements RequestScopeAttribute {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String userId = null;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String applicationName = null;

    /**
     * 프로그램 id
     * <pre>
     * 1. web-ui : 화면에서 전달한 menu-id를 bff에서 x-pgm-id에 설정하여 참조
     * 2. gateway 등 : {gateway-name}::http-method::uri, 최초 생성 후 이후 rest, kafka 등 호출시 전달 됨
     * </pre>
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String programId = EMPTY;

    /**
     * 개인정보 로깅용 클라이언트 IP
     */
    @Setter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String clientIp = UNDEFINED_IP;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> privateClaims = new LinkedCaseInsensitiveMap<>();

    @Getter
    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> customAttributes = new LinkedCaseInsensitiveMap<>();

    @JsonIgnore
    private Map<String, Object> localAttributes = new LinkedCaseInsensitiveMap<>();

    @Setter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String gtid = "";

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Level logLevel;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> forwardedService = new ArrayList<>();

    public ScopeAttribute() {
    }

    // 복사 생성자 (기존 clone() 동작 이전)
    public ScopeAttribute(ScopeAttribute source) {
        // read only
        this.privateClaims = source.privateClaims;

        try {
            this.customAttributes = cloneMap(source.customAttributes, "customAttributes");
        } catch (Exception e) {
            log.warn("ScopeAttribute customAttributes 복제 오류 무시");
            this.customAttributes = source.customAttributes;
        }

        try {
            this.localAttributes = cloneMap(source.localAttributes, "localAttributes");
        } catch (Exception e) {
            log.warn("ScopeAttribute localAttributes 복제 오류 무시");
            this.localAttributes = source.localAttributes;
        }

        try {
            if (source.logLevel != null)
                this.setLogLevel(source.getLogLevel().toString());
        } catch (Exception e) {
            log.warn("ScopeAttribute LogLevel 복제 오류 무시");
        }
        try {
            this.forwardedService = new ArrayList<>();
            source.forwardedService.forEach(this::addForwardedService);
        } catch (Exception e) {
            log.warn("ScopeAttribute forwardedService 복제 오류 무시");
            this.forwardedService = source.forwardedService;
        }

        // 나머지 단순 필드 복사
        this.userId = source.userId;
        this.applicationName = source.applicationName;
        this.programId = source.programId;
        this.clientIp = source.clientIp;
        this.gtid = source.gtid;
    }

    // 정적 팩토리
    public static ScopeAttribute of(ScopeAttribute source) {
        return new ScopeAttribute(source);
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public String getProgramId() {
        return programId;
    }

    @Override
    public void setProgramId(String programId) {
        this.programId = programId;
    }

    @Override
    public String getClientIp() {
        return clientIp;
    }

    @Override
    public Map<String, Object> getPrivateClaims() {
        return Collections.unmodifiableMap(privateClaims);
    }

    @Override
    @JsonIgnore
    public Object getPrivateClaim(String key) {
        return privateClaims.get(key);
    }

    public void setPrivateClaims(Map<String, Object> privateClaims) {
        if (MapUtils.isNotEmpty(privateClaims)) {
            this.privateClaims.clear();
            this.privateClaims.putAll(privateClaims);
        }
    }

    @JsonIgnore
    public void setPrivateClaims(String privateClaimsString) {

        if (StringUtils.isBlank(privateClaimsString))
            return;

        try {
            new ObjectMapper().readerForUpdating(privateClaims).readValue(privateClaimsString);
        } catch (JsonProcessingException e) {
            log.error("ScopeAttribute setPrivatClaims(String) descrialize error {}", privateClaimsString, e);
        }
    }

    @Override
    public Object getCustomAttribute(String key) {
        return customAttributes.get(key);
    }

    @Override
    public String[] getCustomAttributeKeys() {
        return customAttributes.keySet().toArray(new String[0]);
    }

    public void setCustomAttribute(String key, Object value) {
        customAttributes.put(key, value);
    }

    public final void setCustomAttributes(Map<String, Object> customAttributes) {
        if (MapUtils.isNotEmpty(customAttributes)) {
            this.customAttributes.clear();
            this.customAttributes.putAll(customAttributes);
        }
    }

    @JsonIgnore
    public void setCustomAttributes(String customAttributesJson) {

        if (StringUtils.isBlank(customAttributesJson))
            return;

        try {
            customAttributes.clear();
            new ObjectMapper().readerForUpdating(customAttributes).readValue(customAttributesJson);
        } catch (JsonProcessingException e) {
            log.error("ScopeAttribute setCustomAttributes(String) deserialize error {}", customAttributesJson, e);
        }
    }

    @Override
    @JsonIgnore
    public Map<String, Object> getLocalAttributes() {
        return localAttributes;
    }

    @Override
    @JsonIgnore
    public void setLocalAttribute(String key, Object value) {
        localAttributes.put(key, value);
    }

    @Override
    @JsonIgnore
    public Object getLocalAttribute(String key) {
        return localAttributes.get(key);
    }

    @Override
    public String getGtid() {
        return gtid;
    }

    public List<String> getForwardedService() {
        if (CollectionUtils.isEmpty(forwardedService)) {
            ArrayList<String> list = new ArrayList<>();
            list.add(PropertyUtil.getApplicationName());
            return list;
        }

        return forwardedService;
    }

    public void setForwardedService(List<String> xForwardedService) {
        this.forwardedService.clear();
        try {
            this.forwardedService.addAll(xForwardedService);
            this.forwardedService.add(PropertyUtil.getApplicationName());
        } catch (Exception e) {
            log.error("ScopeAttribute.setForwardedUrl(List<String> forwardedUrl) error : {}", e.getMessage());
        }
    }

    public void setForwardedService(List<String> xForwardedService, String... services) {
        this.forwardedService.clear();
        try {
            this.forwardedService.addAll(xForwardedService);
            if (ArrayUtils.isNotEmpty(services)) {
                this.forwardedService.addAll(Arrays.asList(services));
            }
        } catch (Exception e) {
            log.error("ScopeAttribute.setForwardedUrl(List<String> forwardedUrl) error : {}", e.getMessage());
        }
    }

    @JsonIgnore
    public void setForwardedService(Enumeration<String> serviceEnumeration, String... services) {
        this.forwardedService.clear();
        try {
            while (serviceEnumeration.hasMoreElements()) {
                this.forwardedService.add(serviceEnumeration.nextElement());
            }
            if (ArrayUtils.isNotEmpty(services)) {
                this.forwardedService.addAll(Arrays.asList(services));
            }
        } catch (Exception e) {
            log.error("ScopeAttribute.setForwardedUrl(Enumeration<String> serviceEnumeration) error : {}",
                    e.getMessage());
        }
    }

    @JsonIgnore
    public void setForwardedService(String servicesListString, String... services) {
        this.forwardedService.clear();
        try {

            if (StringUtils.isNotBlank(servicesListString)) {
                List<String> in = Splitter.on(",").trimResults().splitToList(servicesListString);
                this.forwardedService.addAll(in);
            }
            if (ArrayUtils.isNotEmpty(services)) {
                this.forwardedService.addAll(Arrays.asList(services));
            }
        } catch (Exception e) {
            log.error("setForwardedUrl(String servicesListString) error : {}", e.getMessage());
        }
    }

    @JsonIgnore
    public void addForwardedService(String service) {
        this.forwardedService.add(service);
    }

    public final Level getLogLevel() {
        return logLevel;
    }

    public final void setLogLevel(String logLevel) {
        if (StringUtils.isBlank(logLevel)) {
            return;
        }
        Level level = null;
        char value = logLevel.charAt(0);
        switch (value) {
            case 'D':
                level = Level.DEBUG;
                break;
            case 'I':
                level = Level.INFO;
                break;
            case 'W':
                level = Level.WARN;
                break;
            case 'E':
                level = Level.ERROR;
                break;
            case 'T', 'A':
                level = Level.TRACE;
                break;
            default: /* null */
        }
        this.logLevel = level;
    }

    public String toConnectedDetailString() {
        return String.format("{\"Application\":\"%s\", \"userId\":\"%s\", \"Time\":\"%s\"}",
                PropertyUtil.getApplicationName(),
                userId,
                LocalDateTime.now());
    }

    // 기존 clone() 제거 (S2975 준수)

    private Map<String, Object> cloneMap(Map<String, Object> source, String propertyName) {
        Map<String, Object> cloned = new LinkedCaseInsensitiveMap<>();
        source.forEach((k, v) -> {
            try {
                cloned.put(k, nvl(ObjectUtils.clone(v), v, propertyName, k));
            } catch (Exception e) {
                log.warn("ScopeAttribute.{} 의 속성 {} 키 값객체는 clone를 지원하지 않아 주소 참조 됩니다.", propertyName, k);
                cloned.put(k, v);
            }
        });
        return cloned;
    }

    private Object nvl(Object r, Object o, String p, String k) {
        if (r == null) {
            if (o != null && !(o instanceof String) && !ClassUtils.isPrimitiveOrWrapper(o.getClass())) {
                log.warn("ScopeAttribute 복제시 {} 속성 {} 키의 값객체가 clone를 지원하지 않아 주소 참조 복제됩니다.", p, k);
            }
            return o;
        }
        return r;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        // @formatter:off
        builder.append("ScopeAttribute [userId=").append(userId)
                .append(", forwardedService=").append(forwardedService)
                .append(", gtid=").append(gtid)
                .append(", customAttributes=").append(customAttributes)
                .append(", localAttributes=").append(localAttributes)
                .append(", logLevel=").append(logLevel)
                .append("]");
        return builder.toString();
        // @formatter:on
    }
}