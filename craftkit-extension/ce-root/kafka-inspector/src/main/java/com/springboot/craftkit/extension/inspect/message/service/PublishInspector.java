package com.springboot.craftkit.extension.inspect.message.service;

import com.springboot.craftkit.extension.inspect.message.dto.AllowedPublishMessage;

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
