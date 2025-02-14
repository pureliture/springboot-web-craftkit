package com.teststrategy.multimodule.maven.cm.inspect.message.aop;

import com.teststrategy.multimodule.maven.cm.inspect.message.exception.NotRegisterdSubscribeException;
import com.teststrategy.multimodule.maven.cm.inspect.message.service.SubscribeInspector;
import com.teststrategy.multimodule.maven.sf.framework.util.MessageUtil;
import io.eventuate.tram.messaging.common.ChannelMapping;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Optional;
import java.util.Set;


@Aspect
public class SubscribeInspectorAspect {

    static final String MESSAGE_CODE = "kafka-inspector.not-registered-subscribe";
    static final String DEFAULT_MESSAGE = "Topic ({0}) with Consumer Group ({1}) is not registered as a valid subscribing topic.\n";

    private ChannelMapping channelMapping;
    private SubscribeInspector subscribeInspector;

    public SubscribeInspectorAspect(SubscribeInspector subscribeInspector,
                                    Optional<ChannelMapping> optionalChannelMapping) {
        this.channelMapping = optionalChannelMapping.orElseGet(() -> channel -> channel);
        this.subscribeInspector = subscribeInspector;
    }

    @Pointcut("execution(* io.eventuate.tram.consumer.common.MessageConsumerImplementation.subscribe(..))")
    public void pointcut() {}

    @Before(value = "pointcut()")
    public void before(JoinPoint joinPoint) {

        Object[] args = joinPoint.getArgs();

        String subscribeId = (String) args[0];

        @SuppressWarnings("unchecked")
        Set<String> channels = (Set<String>) args[1];

        channels.forEach(channel -> throwExceptionIfNotAllowed(subscribeId, channel));
    }

    private void throwExceptionIfNotAllowed(String consumerGroup, String channel) {
        String topic = channelMapping.transform(channel);

        if (subscribeInspector.isNotAllowed(consumerGroup, topic)) {

            String message = MessageUtil.getDefaultMessageIfNone(DEFAULT_MESSAGE,
                    MESSAGE_CODE,
                    topic,
                    consumerGroup);

            throw new NotRegisterdSubscribeException(message);
        }
    }
}
