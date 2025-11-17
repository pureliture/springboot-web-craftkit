package com.springboot.craftkit.extension.inspect.message.service;

import com.springboot.craftkit.extension.inspect.message.dto.AllowedPublishMessage;
import com.springboot.craftkit.extension.inspect.message.dto.AllowedPublishMessageMap;
import com.springboot.craftkit.config.properties.TopicInspectorProperties;
import com.springboot.craftkit.framework.logging.LogMarkers;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class PublishInspectorImpl implements PublishInspector {

    private static final Logger log = LoggerFactory.getLogger(PublishInspectorImpl.class);

    private AllowedPublishMessageMap allowedPublishMessageMap = new AllowedPublishMessageMap();
    private boolean suspended = false;

    private final TopicInspectorProperties topicInspectorProperties;

    public PublishInspectorImpl(TopicInspectorProperties topicInspectorProperties) {
        this.topicInspectorProperties = topicInspectorProperties;
    }

    @Override
    public boolean isAllowed(String topic) {

        if (this.suspended) {
            log.info(LogMarkers.REPEATABLE, "Skipping the registration check because the allowed topic list verification failed.");
            return true;
        }

        if (this.topicInspectorProperties.getExcludedTopics().contains(topic)) {
            return true;
        }

        return this.allowedPublishMessageMap.containsKey(topic);
    }

    @Override
    public void setSuspended() {
        if (this.allowedPublishMessageMap.isEmpty()) {
            log.info(LogMarkers.ENVIRONMENT, "Pausing temporarily due to an error while loading the topic publishing list.");
            this.suspended = true;
        }
    }

    @Override
    public void updateMessageMap(List<AllowedPublishMessage> allowedPublishMessageList) {

        this.suspended = false;

        if (CollectionUtils.isEmpty(allowedPublishMessageList)) {
            return;
        }

        if (log.isDebugEnabled() && (allowedPublishMessageList.size() != this.allowedPublishMessageMap.size())) {
            log.debug(LogMarkers.ENVIRONMENT, "Updating the topic publishing list. ({} items -> {} items)", this.allowedPublishMessageMap.size(), allowedPublishMessageList.size());
            allowedPublishMessageList.forEach(dto ->
                    log.debug("Topic publishing: {} {} {}", dto.getInterfaceId(), dto.getTopicName(), dto.getInterfaceName())
            );
        }

        // 새로운 맵을 생성 (NPE 방지)
        this.allowedPublishMessageMap = allowedPublishMessageList.stream()
                .filter(Objects::nonNull)  // dto 자체가 null인 경우 제거
                .filter(dto -> dto.getTopicName() != null)  // topicName이 null인 경우 제거
                .collect(AllowedPublishMessageMap::new, (map, dto) -> map.put(dto.getTopicName(), dto), AllowedPublishMessageMap::putAll);
    }

    @Override
    public String toString() {
        // @formatter:off
        StringBuilder builder = new StringBuilder();
        builder.append("PublishInspectorImpl [")
                .append("allowedPublishMessageMap=").append(allowedPublishMessageMap)
                .append("]");
        return builder.toString();
        // @formatter:on
    }

}
