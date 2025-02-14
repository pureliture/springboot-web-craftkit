package com.teststrategy.multimodule.maven.config;

import com.teststrategy.multimodule.maven.cm.inspect.message.aop.PublishInspectorAspect;
import com.teststrategy.multimodule.maven.cm.inspect.message.aop.SubscribeInspectorAspect;
import com.teststrategy.multimodule.maven.cm.inspect.message.service.PublishInspector;
import com.teststrategy.multimodule.maven.cm.inspect.message.service.SubscribeInspector;
import com.teststrategy.multimodule.maven.config.properties.PublishInspectorProperties;
import com.teststrategy.multimodule.maven.config.properties.SubscribeInspectorProperties;
import com.teststrategy.multimodule.maven.config.properties.TopicInspectorProperties;
import io.eventuate.tram.consumer.common.MessageConsumerImplementation;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.producer.MessageProducer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.DependsOn;

import java.util.Optional;


@Conditional(TopicInspectorConfig.OnProfile.class)
@ConditionalOnProperty(prefix = TopicInspectorProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass({DomainEventPublisher.class, MessageProducer.class, MessageConsumerImplementation.class})
@AutoConfigureAfter({TopicInspectorConfig.class})
@AutoConfiguration
public class EventuateInspectorConfig {

    @Bean
    @DependsOn("allowedTopicsReader")
    @ConditionalOnProperty(prefix = SubscribeInspectorProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public SubscribeInspectorAspect subscribeInspectAspect(
            SubscribeInspector subscribeInspector,
            Optional<ChannelMapping> optionalChannelMapping) {
        return new SubscribeInspectorAspect(subscribeInspector, optionalChannelMapping);
    }

    @Bean
    @DependsOn("allowedTopicsReader")
    @ConditionalOnProperty(prefix = PublishInspectorProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public PublishInspectorAspect publishInspectAspect(PublishInspector publishInspector,
                                                       Optional<ChannelMapping> optionalChannelMapping) {
        return new PublishInspectorAspect(publishInspector, optionalChannelMapping);
    }
}
