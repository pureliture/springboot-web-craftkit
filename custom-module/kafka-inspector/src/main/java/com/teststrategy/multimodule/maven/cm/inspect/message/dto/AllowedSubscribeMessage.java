package com.teststrategy.multimodule.maven.cm.inspect.message.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AllowedSubscribeMessage {

    private String interfaceId;
    private String interfaceName;

    @JsonProperty(required = true)
    private String topicName;

    @JsonProperty(required = true)
    private String consumerGroupName;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AllowedSubscribeMessage [interfaceId=").append(interfaceId)
                .append(", interfaceName=").append(interfaceName)
                .append(", topicName=").append(topicName)
                .append(", consumerGroupName=").append(consumerGroupName)
                .append("]");
        return builder.toString();
    }
}
