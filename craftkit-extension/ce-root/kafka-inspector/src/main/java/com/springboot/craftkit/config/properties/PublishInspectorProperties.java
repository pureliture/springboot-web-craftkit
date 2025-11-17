package com.springboot.craftkit.config.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublishInspectorProperties extends InspectorReaderProperties {

    public static final String PREFIX = TopicInspectorProperties.PREFIX + ".publish";
    private static final Logger log = LoggerFactory.getLogger(PublishInspectorProperties.class);

    public PublishInspectorProperties() {
        super.resourceUrl = "allowed-publish-topics-{application-name}-{profile}";
    }

    public void toLog() {
        super.toLog(log, "[TopicInspector Topic Publish Register Check]");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PublishInspectorProperties")
                .append(super.toString());
        return builder.toString();
    }
}
