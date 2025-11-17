package com.springboot.craftkit.config;


import com.springboot.craftkit.config.properties.TopicInspectorProperties;
import com.springboot.craftkit.extension.inspect.message.service.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;


@Conditional(TopicInspectorConfig.OnProfile.class)
@ConditionalOnProperty(prefix = TopicInspectorProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(TopicInspectorProperties.class)
@EnableScheduling
@AutoConfiguration
public class TopicInspectorConfig {

    private static final Logger log = LoggerFactory.getLogger(TopicInspectorConfig.class);

    @Bean
    public SubscribeInspector subscribeInspector(TopicInspectorProperties topicInspectorProperties) {
        return new SubscriberInspectorImpl(topicInspectorProperties);
    }

    @Bean
    public PublishInspector publishInspector(TopicInspectorProperties topicInspectorProperties) {
        return new PublishInspectorImpl(topicInspectorProperties);
    }

    @Bean
    public AllowedTopicsReader allowedTopicsReader(
            SubscribeInspector subscribeInspector,
            PublishInspector publishInspector,
            TopicInspectorProperties topicInspectorProperties,
            Environment environment) {
        return new AllowedTopicsReader(subscribeInspector, publishInspector, topicInspectorProperties, environment);
    }

    public static class OnProfile implements Condition {

        private final String[] EXCEPTS = {"local", "edu", "prod"};

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Environment environment = context.getEnvironment();

            try {
                String[] profiles = environment.getActiveProfiles();
                if (profiles.length == 0) {
                    profiles = environment.getDefaultProfiles();
                }

                if (ArrayUtils.isEmpty(profiles))
                    return false;

                return Arrays.stream(profiles)
                        .noneMatch(
                                profile -> Arrays.stream(EXCEPTS).anyMatch(arg -> StringUtils.endsWithIgnoreCase(profile, arg)));
            } catch (Exception e) {
                log.warn("OnDevProfile error {}", e.getMessage());
                return false;
            }
        }
    }
}
