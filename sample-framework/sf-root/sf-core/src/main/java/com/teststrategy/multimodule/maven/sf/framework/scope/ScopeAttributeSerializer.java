package com.teststrategy.multimodule.maven.sf.framework.scope;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.common.base.Joiner;
import com.teststrategy.multimodule.maven.sf.framework.util.HttpUtil;
import com.teststrategy.multimodule.maven.sf.framework.util.PropertyUtil;
import com.teststrategy.multimodule.maven.sf.framework.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

import static com.teststrategy.multimodule.maven.sf.framework.scope.RequestScopeAttribute.*;

/**
 * RequestScopeAttribute 를 http header 또는 kafka Header 로 전달한다
 */
@Slf4j
public class ScopeAttributeSerializer {

    public ScopeAttribute scopeAttribute;

    public ScopeAttributeSerializer() {
        this.scopeAttribute = (ScopeAttribute) RequestScopeUtil.getAttribute();
    }

    protected static final ObjectMapper objectMapper = new ObjectMapper();
    protected static ObjectWriter httpWriter;
    protected static ObjectWriter kafkaWriter;

    static {
        objectMapper.findAndRegisterModules();
        SimpleBeanPropertyFilter httpPropertyFilter = SimpleBeanPropertyFilter.serializeAllExcept("gtid", "forwardedService");
        FilterProvider httpFilterProvider = new SimpleFilterProvider().addFilter("scopeAttributeJsonFilter", httpPropertyFilter);
        httpWriter = objectMapper.writer(httpFilterProvider);

        SimpleBeanPropertyFilter kafkaPropertyFilter = SimpleBeanPropertyFilter.serializeAll();
        FilterProvider kafkaFilterProvider = new SimpleFilterProvider().addFilter("scopeAttributeJsonFilter", kafkaPropertyFilter);
        kafkaWriter = objectMapper.writer(kafkaFilterProvider);
    }

    public void setKafkaHeader(Map<String, String> maps) {

        addHeader(maps::putIfAbsent, null, GLOBAL_TRANSACTION_ID, getGtidForKafka(maps));
        addHeader(maps::putIfAbsent, null, FORWARDED_SERVICE, Joiner.on(',').join(scopeAttribute.getForwardedService()));
        setHeadersOfHttpOrKafka(maps::putIfAbsent, null);
        log.debug("RequestScopeAttribute is serialized for kafka Request {}", maps);
    }

    /**
     * kafka용 gtid를 추출 또는 생성
     * <pre>
     *  1. scopeAttribute의 gtid
     *  2. message header의 ID
     *  3. HttpUtil.generateNewGtid() 으로 생성
     * </pre>
     */
    private String getGtidForKafka(Map<String, String> maps) {

        String gtid = scopeAttribute.getGtid();
        if (StringUtils.isNotBlank(gtid))
            return gtid;

        gtid = maps.get("ID");
        if (StringUtils.isNotBlank(gtid))
            return gtid;

        return HttpUtil.generateNewGtid();
    }

    public void setHttpHeaders(HttpHeaders httpHeaders) {
        setHeadersOfHttpOrKafka(httpHeaders::addIfAbsent, StringUtil::encodeURIComponent);
    }

    private void setHeadersOfHttpOrKafka(BiConsumer<String, String> consumer, UnaryOperator<String> valueConverter) {
        addHeader(consumer, valueConverter, USER_ID, scopeAttribute.getUserId());
        // app-name 를 필수로 설정하며, app-name 은 직접 호출하는 서비스(즉, 바로 직전 서비스명이다. 2021-10-28)
        addHeader(consumer, valueConverter, APPLICATION_NAME, PropertyUtil.getApplicationName());
        addHeader(consumer, valueConverter, PROGRAM_ID, scopeAttribute.getProgramId());
        addHeader(consumer, valueConverter, PRIVATE_CLAIMS, getJsonString(scopeAttribute.getPrivateClaims()));
        addHeader(consumer, valueConverter, ScopeAttribute.LOG_LEVEL, ObjectUtils.defaultIfNull(scopeAttribute.getLogLevel(), "").toString());

        /*
         * customAttribute 의 속성중 Http 헤더에 직접 표현하는 속성(X-Custom-Attributes에 넣지 않고) 처리
         * - X-MRKT-ID, X-REQ-HEADER 포함
         */
        Map<String, Object> newCustomAttribute = new HashMap<>();
        scopeAttribute.getCustomAttributes().forEach((name, value) -> {
            if (ExposedCustomAttributeNames.isNotContains(name)) {
                newCustomAttribute.put(name, value);
            } else {
                addHeader(consumer, valueConverter, name, value instanceof String ? (String) value : getJsonString(value));
            }
        });

        addHeader(consumer, valueConverter, CUSTOM_ATTRIBUTES, getJsonString(newCustomAttribute));
    }

    private void addHeader(BiConsumer<String, String> consumer, UnaryOperator<String> valueConverter, String name, String value) {

        if (StringUtils.isBlank(value))
            return;

        consumer.accept(name, (valueConverter == null ? value : valueConverter.apply(value)));
    }

    private String getJsonString(Object value) {

        if(ObjectUtils.isEmpty(value)){
            return null;
        }

        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("RequestScopeAttribute map attribute serialize error {}", value, e);
            return null;
        }
    }

    /**
     * customAttribute map 의 값을 변경하여 json serialize 하고 uriEncoding한다
     */
    public String getModifiedCustomAttributeJson(String name, Object value) {

        Map<String, Object> origin = scopeAttribute.getCustomAttributes();
        Map<String, Object> copy = new LinkedCaseInsensitiveMap<>();

        if (!MapUtils.isEmpty(origin)) {
            for (Map.Entry<String, Object> entry : origin.entrySet()) {
                copy.put(entry.getKey(), entry.getValue());
            }
        }
        copy.put(name, value);

        log.debug("X-Custom-Attribute serialized {}", copy);

        return StringUtil.encodeURIComponent(getJsonString(copy));
    }
}
