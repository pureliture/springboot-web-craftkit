package com.teststrategy.multimodule.maven.config.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SubscribeInspectorProperties extends InspectorReaderProperties {

    public static final String PREFIX = TopicInspectorProperties.PREFIX + ".subscribe";
    private static final Logger log = LoggerFactory.getLogger(SubscribeInspectorProperties.class);

    public SubscribeInspectorProperties() {
        super.resourceUrl = "allowed-subscribe-topics-{application-name}-{profile}";
    }

    public void toLog() {
        super.toLog(log, "[TopicInspector Topic Subscription Register Check]");
    }

    @Override
    public String toString() {
        // @formatter:off
        StringBuilder builder = new StringBuilder();
        builder.append("SubscribeInspectorProperties")
                .append(super.toString());
        return builder.toString();
        // @formatter:on
    }
}
