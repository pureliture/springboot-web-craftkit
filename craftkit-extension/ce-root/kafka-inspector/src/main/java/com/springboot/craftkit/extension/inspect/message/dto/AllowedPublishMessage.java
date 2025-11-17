package com.springboot.craftkit.extension.inspect.message.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AllowedPublishMessage {

    private String interfaceId;
    private String interfaceName;

    @JsonProperty(required = true)
    private String topicName;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AllowedPublishMessage [interfaceId=").append(interfaceId)
                .append(", interfaceName=").append(interfaceName)
                .append(", topicName=").append(topicName)
                .append("]");

        return builder.toString();
    }
}
