package com.teststrategy.multimodule.maven.cm.inspect.message.service;

import com.teststrategy.multimodule.maven.cm.inspect.message.dto.AllowedSubscribeMessage;
import com.teststrategy.multimodule.maven.cm.inspect.message.dto.AllowedSubscribeMessageMap;
import com.teststrategy.multimodule.maven.config.properties.TopicInspectorProperties;
import com.teststrategy.multimodule.maven.sf.framework.logging.LogMarkers;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class SubscriberInspectorImpl implements SubscribeInspector {

    private static final Logger log = LoggerFactory.getLogger(SubscriberInspectorImpl.class);

    private AllowedSubscribeMessageMap allowedSubscribeMessageMap = new AllowedSubscribeMessageMap();

    private boolean suspended = false;

    private final TopicInspectorProperties topicInspectorProperties;

    public SubscriberInspectorImpl(TopicInspectorProperties topicInspectorProperties) {
        this.topicInspectorProperties = topicInspectorProperties;
    }


    @Override
    public boolean isAllowed(String consumerGroup, String topic) {

        if (this.suspended) {
            log.info(LogMarkers.REPEATABLE, "Skipping the registration check because the allowed topic list verification failed.");
            return true;
        }

        if(this.topicInspectorProperties.getExcludedTopics().contains(topic)) {
            return true;
        }

        return allowedSubscribeMessageMap.containsKey(consumerGroup, topic);
    }

    @Override
    public void setSuspended() {
        if (allowedSubscribeMessageMap.isEmpty()) {
            log.info(LogMarkers.ENVIRONMENT, "Pausing temporarily due to an error while loading the topic subscribing list.");
            this.suspended = true;
        }
    }


    @Override
    public void updateMshMap(List<AllowedSubscribeMessage> allowedSubscribeMessageList) {

        this.suspended = false;

        if (CollectionUtils.isEmpty(allowedSubscribeMessageList)) {
            return;
        }

        if (log.isDebugEnabled() && (allowedSubscribeMessageList.size() != this.allowedSubscribeMessageMap.size())) {
            log.debug(LogMarkers.ENVIRONMENT, "Updating the topic subscribing list. ({} items -> {} items) {}",
                    this.allowedSubscribeMessageMap.size(), allowedSubscribeMessageList.size(), allowedSubscribeMessageList);
            allowedSubscribeMessageList.forEach(dto ->
                    log.debug("Topic subscribing : {} {} {} {}", dto.getInterfaceId(), dto.getTopicName(), dto.getConsumerGroupName(), dto.getInterfaceName()));
        }

        // 새로운 맵을 생성 (NPE 방지)
        this.allowedSubscribeMessageMap = allowedSubscribeMessageList.stream()
                .filter(Objects::nonNull)  // dto 자체가 null인 경우 제거
                .filter(dto -> !(dto.getTopicName() == null && dto.getConsumerGroupName() == null))
                .collect(AllowedSubscribeMessageMap::new, (map, dto) -> map.put(dto.getConsumerGroupName(), dto.getTopicName(), dto),
                        AllowedSubscribeMessageMap::putAll);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SubscriberInspectorImpl [")
                .append("allowedSubscribeMessageMap=").append(allowedSubscribeMessageMap)
                .append("]");

        return builder.toString();
    }

}
