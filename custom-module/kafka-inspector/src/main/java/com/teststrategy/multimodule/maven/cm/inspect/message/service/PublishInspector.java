package com.teststrategy.multimodule.maven.cm.inspect.message.service;

import com.teststrategy.multimodule.maven.cm.inspect.message.dto.AllowedPublishMessage;

import java.util.List;

public interface PublishInspector {


    default boolean isAllowed(String topic) {
        return true;
    }

    default boolean isNotAllowed(String topic) {
        return !isAllowed(topic);
    }

    void updateMessageMap(List<AllowedPublishMessage> allowedPublishMessageList);

    void setSuspended();
}
