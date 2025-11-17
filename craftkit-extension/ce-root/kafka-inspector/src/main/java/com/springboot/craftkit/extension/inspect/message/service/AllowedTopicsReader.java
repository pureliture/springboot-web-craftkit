package com.springboot.craftkit.extension.inspect.message.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.craftkit.extension.inspect.message.dto.AllowedPublishMessage;
import com.springboot.craftkit.extension.inspect.message.dto.AllowedSubscribeMessage;
import com.springboot.craftkit.config.properties.TopicInspectorProperties;
import com.springboot.craftkit.framework.logging.LogMarkers;
import com.springboot.craftkit.framework.util.PropertyUtil;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

import static java.lang.Math.random;


public class AllowedTopicsReader implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(AllowedTopicsReader.class);

    private final ObjectMapper objectMapper;
    private final SubscribeInspector subscribeInspector;
    private final PublishInspector publishInspector;

    private String subscribeTopicListUrl;
    private String publishTopicListUrl;

    private final Environment environment;
    private final TopicInspectorProperties topicInspectorProperties;


    public AllowedTopicsReader(SubscribeInspector subscribeInspector, PublishInspector publishInspector, TopicInspectorProperties topicInspectorProperties, Environment environment) {

        this.objectMapper = new ObjectMapper().findAndRegisterModules();
        this.subscribeInspector = subscribeInspector;
        this.publishInspector = publishInspector;
        this.topicInspectorProperties = topicInspectorProperties;
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        topicInspectorProperties.setApplicationName(environment.getProperty("spring.application.name"));

        if (topicInspectorProperties.isNotValidPublishListUrl()) {
            String message = "[Allowed Publish Topic List Verification] Invalid URL And Parameter (" + topicInspectorProperties.getParsedPublishListUrl() + ")";
            throw new IllegalAccessException(message);
        }

        if (topicInspectorProperties.isNotValidSubscribeListUrl()) {
            String message = "[Allowed Subscribe Topic List Verification] Invalid URL And Parameter (" + topicInspectorProperties.getParsedSubscribeListUrl() + ")";
            throw new IllegalAccessException(message);
        }

        log.info(LogMarkers.ENVIRONMENT, "Check publish topic List");
        this.publishTopicListUrl = this.topicInspectorProperties.getParsedPublishListUrl();
        this.updatePublisherAllowedList();

        log.info(LogMarkers.ENVIRONMENT, "Check subscribe topic topic List");
        this.subscribeTopicListUrl = topicInspectorProperties.getParsedSubscribeListUrl();
        this.updateSubscriberAllowedList();
    }

    public void updateSubscriberAllowedList() {
        try {
            List<AllowedSubscribeMessage> messageList = getAllowedList(subscribeTopicListUrl, new TypeReference<List<AllowedSubscribeMessage>>() {});
            log.debug("subscribe topic List :\nsize : {} ({})", messageList.size(), subscribeTopicListUrl);
            if (CollectionUtils.isNotEmpty(messageList)) {
                subscribeInspector.updateMshMap(messageList);
            }
        } catch (Exception e) {
            log.debug(LogMarkers.ENVIRONMENT, "Fail to read subscribe topic List", e);
            subscribeInspector.setSuspended();
        }
    }

    private void updatePublisherAllowedList() {
        try {
            List<AllowedPublishMessage> messageList = getAllowedList(publishTopicListUrl, new TypeReference<List<AllowedPublishMessage>>() {});
            log.debug("publish topic List :\nsize : {} ({})", messageList.size(), publishTopicListUrl);
            if (CollectionUtils.isNotEmpty(messageList)) {
                publishInspector.updateMessageMap(messageList);
            }
        } catch (Exception e) {
            log.debug(LogMarkers.ENVIRONMENT, "Fail to read publish topic List", e);
            publishInspector.setSuspended();
        }
    }

    @SneakyThrows
    private <T> List<T> getAllowedList(String url, TypeReference<List<T>> typeReference) {
        Resource resource = PropertyUtil.getResource(url);
        return objectMapper.readValue(resource.getInputStream(), typeReference);
    }

    @Scheduled(cron = "${" + TopicInspectorProperties.PREFIX + ".update-cron:0 0 0/2 * * *}")
    public void scheduleUpdateAllowedList() {

        try {
            long sleepTime = (long) (random() * 180.0 * 1000);
            log.debug("Topic Inspector cron started with {} seconds delay !!!", (sleepTime/1000));
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            log.error("Failed to sleep thread\n{}", e.getLocalizedMessage()); // ignore
        }

        this.updatePublisherAllowedList();
        this.updateSubscriberAllowedList();
    }
}
