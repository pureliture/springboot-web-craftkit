package com.springboot.craftkit.config;


import com.springboot.craftkit.extension.inspect.message.aop.KafkaStreamInspectorAspect;
import com.springboot.craftkit.extension.inspect.message.service.PublishInspector;
import com.springboot.craftkit.extension.inspect.message.service.SubscribeInspector;
import com.springboot.craftkit.config.properties.TopicInspectorProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.DependsOn;


@Conditional(TopicInspectorConfig.OnProfile.class)
@ConditionalOnBean(BindingServiceProperties.class)
@ConditionalOnClass({org.springframework.cloud.stream.binding.BindingService.class})
@ConditionalOnProperty(prefix = TopicInspectorProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter({TopicInspectorConfig.class})
@AutoConfiguration
public class KafkaStreamInspectorConfig {

    @Bean
    @DependsOn("allowedTopicsReader")
    @ConditionalOnProperty(prefix = TopicInspectorProperties.PREFIX, name = "kafka-stream.enabled", havingValue = "true", matchIfMissing = true)
    public KafkaStreamInspectorAspect kafkaStreamInspectorAspect(BindingServiceProperties bindingServiceProperties,
                                                                 SubscribeInspector subscribeInspector,
                                                                 PublishInspector publishInspector,
                                                                 TopicInspectorProperties topicInspectorProperties) {
        return new KafkaStreamInspectorAspect(
                bindingServiceProperties,
                subscribeInspector,
                publishInspector,
                topicInspectorProperties);
    }
}
