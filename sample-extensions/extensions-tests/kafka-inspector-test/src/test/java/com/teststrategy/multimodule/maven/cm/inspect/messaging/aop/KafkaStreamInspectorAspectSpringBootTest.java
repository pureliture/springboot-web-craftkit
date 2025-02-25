package com.teststrategy.multimodule.maven.cm.inspect.messaging.aop;

import com.teststrategy.multimodule.maven.cm.inspect.message.exception.NotRegisteredPublishException;
import com.teststrategy.multimodule.maven.cm.inspect.message.exception.NotRegisteredSubscribeException;
import com.teststrategy.multimodule.maven.config.context.MockStaticTestExecutionListener;
import com.teststrategy.multimodule.maven.config.SpringCloudStreamBindModifier;
import com.teststrategy.multimodule.maven.config.SpringCloudStreamTestConfig;
import com.teststrategy.multimodule.maven.config.TopicInspectorTestPresetConfig;
import com.teststrategy.multimodule.maven.config.context.TestExecutionCondition;
import com.teststrategy.multimodule.maven.config.properties.TopicInspectorProperties;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.Binding;
import org.springframework.cloud.stream.binding.BindingService;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.*;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;


/**
 * {@code KafkaStreamInspectorAspectSpringBootTest} is a test class that verifies
 * the dynamic binding modification functionality of Spring Cloud Stream in a Kafka-based
 * messaging environment.
 *
 * <p>
 * This class is executed in a Spring Boot Test environment and validates whether
 * {@link SpringCloudStreamBindModifier} correctly modifies stream bindings (destinations)
 * and restores them to their original configurations.
 * </p>
 *
 * <h2>Test Scenarios:</h2>
 * <ul>
 *     <li>Ensuring that producer and consumer bindings are modified correctly and restored afterward.</li>
 *     <li>Validating that an exception is thrown when attempting to use an unregistered topic.</li>
 * </ul>
 *
 * <h2>Profile-Specific Test Execution:</h2>
 * <ul>
 *     <li>{@code @EnabledIfSystemProperty(named = "spring.profiles.active", matches = "test")}:<br>
 *         Ensures that this test runs only when {@code -Dspring.profiles.active=test} is set.</li>
 *     <li>{@code @ActiveProfiles("test")}:<br>
 *         Explicitly activates the {@code test} profile for this test class.</li>
 *     <li>Externalized Configuration via {@code application-test.yml}:<br>
 *         This file contains environment-specific properties used during the test execution.</li>
 * </ul>
 *
 * <p>
 * If this test is executed without the {@code test} profile, it will be automatically skipped.
 * </p>
 */
@ExtendWith(TestExecutionCondition.class)
@ActiveProfiles("test")
@TestExecutionListeners(
        listeners = {MockStaticTestExecutionListener.class},
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
@ContextConfiguration(classes = {SpringCloudStreamTestConfig.class, TopicInspectorTestPresetConfig.class})
@SpringBootTest
class KafkaStreamInspectorAspectSpringBootTest {

    @Autowired
    private TopicInspectorProperties topicInspectorProperties;

    @Autowired
    private BindingService bindingService;

    @Autowired
    private SpringCloudStreamBindModifier springCloudStreamBindModifier;

    @BeforeEach
    void setUp() {
        springCloudStreamBindModifier.initializeBindingService();
    }

    /**
     * AOP test for verifying producer binding to a permitted topic.
     *
     * @see org.springframework.cloud.stream.binding.BindingService#bindProducer(Object, String)
     * @see com.teststrategy.multimodule.maven.cm.inspect.message.aop.KafkaStreamInspectorAspect#beforeBindProducer(JoinPoint)
     */
    @DisplayName("Allow producer binding to a predefined output channel with a permitted topic")
    @Test
    void testBindProducer_givenPermittedTopic_whenBindingProducer_thenSuccess() {

        /* Given */
        String functionName = "publish";
        String permittedTopic = "sample_topic";

        /* When */
        Binding<?> binding = springCloudStreamBindModifier.changeProducerFunctionBindingsDestination(
                functionName, SpringCloudStreamBindModifier.BindingChannel.OUT, 0, permittedTopic);

        /* Then */
        assertEquals(binding.getBindingName(), permittedTopic);
    }

    /**
     * AOP test for verifying that an exception is thrown when binding a producer to a restricted topic.
     *
     * @see org.springframework.cloud.stream.binding.BindingService#bindProducer(Object, String)
     * @see com.teststrategy.multimodule.maven.cm.inspect.message.aop.KafkaStreamInspectorAspect#beforeBindProducer(JoinPoint)
     */
    @DisplayName("Throw an exception when binding a predefined output channel producer to a restricted topic")
    @Test
    void testBindProducer_givenRestrictedTopic_whenBindingProducer_thenThrowException() {

        /* Given */
        String functionName = "publish";
        String restrictedTopic = "sample_unregistered_topic";

        /* When, Then */
        assertThrows(NotRegisteredPublishException.class,
                () -> springCloudStreamBindModifier.changeProducerFunctionBindingsDestination(
                        functionName, SpringCloudStreamBindModifier.BindingChannel.OUT, 0, restrictedTopic));
    }

    /**
     * AOP test for verifying consumer binding to a permitted topic.
     *
     * @see org.springframework.cloud.stream.binding.BindingService#bindConsumer(Object, String)
     * @see com.teststrategy.multimodule.maven.cm.inspect.message.aop.KafkaStreamInspectorAspect#beforeBindConsumer(JoinPoint)
     */
    @DisplayName("Allow consumer binding to a predefined input channel with a permitted topic")
    @Test
    void testBindConsumer_givenPermittedTopic_whenBindingConsumer_thenSuccess() {

        /* Given */
        String functionName = "subscribe";
        String permittedTopic = "sample_topic";

        /* When */
        Collection<Binding<MessageChannel>> bindingList = springCloudStreamBindModifier.changeConsumerFunctionBindingsDestination(
                functionName, SpringCloudStreamBindModifier.BindingChannel.IN, 0, permittedTopic);

        /* Then */
        assertNotNull(bindingList);
        assertTrue(bindingList.stream().anyMatch(binding -> StringUtils.equals(binding.getBindingName(), permittedTopic)));
    }

    /**
     * AOP test for verifying that an exception is thrown when binding a consumer to a restricted topic.
     *
     * @see org.springframework.cloud.stream.binding.BindingService#bindConsumer(Object, String)
     * @see com.teststrategy.multimodule.maven.cm.inspect.message.aop.KafkaStreamInspectorAspect#beforeBindConsumer(JoinPoint)
     */
    @DisplayName("Throw an exception when binding a predefined input channel consumer to a restricted topic")
    @Test
    void testBindConsumer_givenRestrictedTopic_whenBindingConsumer_thenThrowException() {

        /* Given */
        String functionName = "subscribe";
        String restrictedTopic = "sample_unregistered_topic";

        /* When, Then */
        assertThrows(NotRegisteredSubscribeException.class,
                (() -> springCloudStreamBindModifier.changeConsumerFunctionBindingsDestination(
                        functionName, SpringCloudStreamBindModifier.BindingChannel.IN, 0, restrictedTopic)));
    }
}