package com.teststrategy.multimodule.maven.cm.inspect.message.aop;

import com.teststrategy.multimodule.maven.cm.inspect.message.exception.NotRegisterdSubscribeException;
import com.teststrategy.multimodule.maven.cm.inspect.message.exception.NotRegisteredPublishException;
import com.teststrategy.multimodule.maven.cm.inspect.message.service.PublishInspector;
import com.teststrategy.multimodule.maven.cm.inspect.message.service.SubscribeInspector;
import com.teststrategy.multimodule.maven.config.properties.TopicInspectorProperties;
import com.teststrategy.multimodule.maven.sf.framework.util.MessageUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.util.StringUtils;


@Aspect
public class KafkaStreamInspectorAspect {

    private static final String SUB_MESSAGE_CODE = SubscribeInspectorAspect.MESSAGE_CODE;
    private static final String SUB_DEFAULT_MESSAGE = SubscribeInspectorAspect.DEFAULT_MESSAGE;

    private static final String PUB_MESSAGE_CODE = PublishInspectorAspect.MESSAGE_CODE;
    private static final String PUB_DEFAULT_MESSAGE = PublishInspectorAspect.DEFAULT_MESSAGE;

    private final SubscribeInspector subscribeInspector;
    private final PublishInspector publishInspector;
    private final TopicInspectorProperties topicInspectorProperties;
    private final BindingServiceProperties bindingServiceProperties;

    private static final Logger log = LoggerFactory.getLogger(KafkaStreamInspectorAspect.class);

    public KafkaStreamInspectorAspect(BindingServiceProperties bindingServiceProperties,
                                        SubscribeInspector subscribeInspector,
                                        PublishInspector publishInspector,
                                        TopicInspectorProperties topicInspectorProperties) {
        this.bindingServiceProperties = bindingServiceProperties;
        this.subscribeInspector = subscribeInspector;
        this.publishInspector = publishInspector;
        this.topicInspectorProperties = topicInspectorProperties;
    }

    /**
     * @see org.springframework.cloud.stream.binding.BindingService#bindProducer(Object, String)
     */
    @Pointcut("execution(* org.springframework.cloud.stream.binding.BindingService.bindConsumer(..))")
    public void bindConsumer() {}

    @Before(value = "bindConsumer()")
    public void beforeBindConsumer(JoinPoint joinPoint) throws Throwable {

        if (topicInspectorProperties.getSubscribe().isDisabled())
            return;

        Object[] args = joinPoint.getArgs();
        String channel = (String) args[1];
        BindingProperties bindingProperties = bindingServiceProperties.getBindingProperties(channel);

        String bindingTarget = bindingProperties.getDestination();
        String consumerGroup = bindingProperties.getGroup();
        String[] bindingTargets = StringUtils.commaDelimitedListToStringArray(bindingTarget);
        log.info("consume : {} {} {}", channel, bindingTarget, consumerGroup);

        for (String topic : bindingTargets) {
            this.throwExceptionIfNotAllowedSubscribe(channel, consumerGroup, topic);
        }
    }

    private void throwExceptionIfNotAllowedSubscribe(String channel, String consumerGroup, String topic) {

        String message = MessageUtil.getDefaultMessageIfNone(SUB_DEFAULT_MESSAGE,
                SUB_MESSAGE_CODE,
                topic,
                consumerGroup);

        if (subscribeInspector.isNotAllowed(consumerGroup, topic)) {
            throw new NotRegisterdSubscribeException(message);
        }
    }

    /**
     * @see org.springframework.cloud.stream.binding.BindingService#bindProducer(Object, String)
     */
    @Pointcut("execution(* org.springframework.cloud.stream.binding.BindingService.bindProducer(..))")
    public void bindProducer() {}

    @Before(value = "bindProducer()")
    public void beforeBindProducer(JoinPoint joinPoint) throws Throwable {

        if (topicInspectorProperties.getPublish().isDisabled())
            return;

        Object[] args = joinPoint.getArgs();
        String channel = (String) args[1];
        BindingProperties bindingProperties = bindingServiceProperties.getBindingProperties(channel);

        String topic = bindingProperties.getDestination();
        log.info("produce : {} {}", channel, topic);

        this.throwExceptionIfNotAllowedPublish(channel, topic);
    }

    private void throwExceptionIfNotAllowedPublish(String channel, String topic) {

        if (publishInspector.isNotAllowed(topic)) {

            String message = MessageUtil.getDefaultMessageIfNone(
                    PUB_DEFAULT_MESSAGE,
                    PUB_MESSAGE_CODE,
                    topic);

            throw new NotRegisteredPublishException(message);
        }
    }
}
