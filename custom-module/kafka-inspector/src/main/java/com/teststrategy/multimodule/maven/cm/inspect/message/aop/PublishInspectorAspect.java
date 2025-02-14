package com.teststrategy.multimodule.maven.cm.inspect.message.aop;

import com.teststrategy.multimodule.maven.cm.inspect.message.exception.NotRegisteredPublishException;
import com.teststrategy.multimodule.maven.cm.inspect.message.service.PublishInspector;
import com.teststrategy.multimodule.maven.sf.framework.util.MessageUtil;
import io.eventuate.tram.messaging.common.ChannelMapping;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Optional;

@Aspect
public class PublishInspectorAspect {

    static final String MESSAGE_CODE = "kafka-inspector.not-registered-topic";
    static final String DEFAULT_MESSAGE = "Topic ({0}) is not registered as a valid publishing topic.";

    private ChannelMapping channelMapping;
    private PublishInspector publishInspector;

    public PublishInspectorAspect(PublishInspector publishInspector,
                                  Optional<ChannelMapping> optionalChannelMapping) {
        this.channelMapping = optionalChannelMapping.orElseGet(() -> channel -> channel);
        this.publishInspector = publishInspector;
    }

    @Pointcut("execution(* io.eventuate.tram.events.publisher.DomainEventPublisher.publish(..))")
    public void publish() {}

    @Pointcut("execution(* io.eventuate.tram.messaging.producer.MessageProducer.send(..))")
    public void send() {}

    @Before(value = "publish() || send()")
    public void before(JoinPoint joinPoint) {

        Object[] args = joinPoint.getArgs();

        String destination = (String) args[0];
        throwExceptionIfNotAllowed(destination);
    }

    private void throwExceptionIfNotAllowed(String channel) {
        String topic = channelMapping.transform(channel);
        if (!publishInspector.isAllowed(topic)) {

            String message = MessageUtil.getDefaultMessageIfNone(
                    DEFAULT_MESSAGE,
                    MESSAGE_CODE,
                    topic);

            throw new NotRegisteredPublishException(message);
        }
    }
}
