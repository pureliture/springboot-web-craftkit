package com.teststrategy.multimodule.maven.config.properties;


import com.teststrategy.multimodule.maven.sf.framework.logging.LogMarkers;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

@ConfigurationProperties(prefix = TopicInspectorProperties.PREFIX)
public class TopicInspectorProperties {

    public static final String PREFIX = "sample-framework.custom-module.inspector.topic";
    private static final Logger log = LoggerFactory.getLogger(TopicInspectorProperties.class);

    @Setter
    @Getter
    private boolean enabled = true;

    @Getter
    private String applicationName;

    @Setter
    private String updateCron;

    @Setter
    @Getter
    private PublishInspectorProperties publish = new PublishInspectorProperties();

    @Setter
    @Getter
    private SubscribeInspectorProperties subscribe = new SubscribeInspectorProperties();

    @Getter
    private Set<String> excludedTopics = new HashSet<>();

    public TopicInspectorProperties() {
        excludedTopics = new HashSet<>();
    }


    /**
     * spring.application.name 에서 시스템코드으로 등록하며, 만약 application.yml에서
     * 설정이 있다면 변경하지 않는다.
     */
    public void setApplicationName(String applicationName) {
        if (StringUtils.isBlank(this.applicationName)) {
            this.applicationName = applicationName;
        }
    }

    public synchronized void setExcludedTopics(Set<String> excludedTopics) {
        this.excludedTopics.addAll(excludedTopics);
    }

    public boolean isValidSubscribeListUrl() {
        return subscribe.isValidUrlAndParams(this.applicationName);
    }

    public boolean isNotValidSubscribeListUrl() {
        return !this.isValidSubscribeListUrl();
    }

    public boolean isValidPublishListUrl() {
        return publish.isValidUrlAndParams(this.applicationName);
    }

    public boolean isNotValidPublishListUrl() {
        return !this.isValidPublishListUrl();
    }

    public String getParsedSubscribeListUrl() {
        return subscribe.getParsedResourceUrl(this.applicationName);
    }

    public String getParsedPublishListUrl() {
        return publish.getParsedResourceUrl(this.applicationName);
    }

    public void toLog() {
        log.info(LogMarkers.ENVIRONMENT, "[TopicInspector Activation Check] enabled={}", enabled);
        log.info(LogMarkers.ENVIRONMENT, "[TopicInspector Activation Check] application-name={}", applicationName);
        log.info(LogMarkers.ENVIRONMENT, "[TopicInspector Activation Check] update-cron={}", updateCron);
        subscribe.toLog();
        publish.toLog();
    }

    @Override
    public String toString() {
        // @formatter:off
        StringBuilder builder = new StringBuilder();
        builder.append("TopicInspectorProperties")
                .append(" [enabled=") .append(enabled)
                .append(", applicationName=") .append(applicationName)
                .append(", updateCron=") .append(updateCron)
                .append(", publish=") .append(publish)
                .append(", subscribe=") .append(subscribe)
                .append("]");
        return builder.toString();
        // @formatter:on
    }
}
