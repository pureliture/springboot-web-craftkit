package com.springboot.craftkit.framework.application.setting;

import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

/**
 * static resource, actuator 등의 url 에 대해 추가적인 필터를 거치지 않고 바로 dispatcher servlet 로 전달하기 위한 필터
 */
@ToString
@ConfigurationProperties(prefix = StaticResourcesFilterProperties.PREFIX)
public class StaticResourcesFilterProperties {

    public static final String PREFIX = "craftkit.static-resources-filter";

    /**
     *  /actuator/*, /swagger-ui.html, /v2/api-docs, /swagger-ui, /swagger-ui/*, /v3/api-docs, /v3/api-docs/* 은 자동등록 된다.
     *  단, /swagger-ui.html, /v2/api-docs, /swagger-ui, /swagger-ui/*, /v3/api-docs, /v3/api-docs/* 운영환경에서는 제외된다.
     */
    private final Set<String> urls = new HashSet<>();

    public StaticResourcesFilterProperties() {
        super();
    }

    public final Set<String> getUrls() {
        return urls;
    }

    public final void setUrls(Set<String> urls) {
        this.urls.addAll(urls);
    }

    public final void addUrl(String url) {
        this.urls.add(url);
    }

}
