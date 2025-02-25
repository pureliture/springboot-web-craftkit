package com.teststrategy.multimodule.maven.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.cloud.stream.binder.Binding;
import org.springframework.cloud.stream.binding.BindingService;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A utility Bean class for dynamically modifying and restoring Spring Cloud Stream bindings.
 * <p>
 * This class provides functionality to change destinations during test execution
 * and restore them to their initial state before each test.
 * </p>
 */
public class SpringCloudStreamBindModifier {

    private final Logger log = LoggerFactory.getLogger(SpringCloudStreamBindModifier.class);

    private final BindingService bindingService;
    private final ConfigurationPropertiesBindingPostProcessor bindingPostProcessor;

    /**
     * Constructor for SpringCloudStreamBindModifier.
     * <p>
     * It retrieves the initial producer and consumer binding information from the BindingService
     * and stores it to enable restoration to the original state when destinations are modified during tests.
     * </p>
     */
    public SpringCloudStreamBindModifier(BindingService bindingService,
                                         ConfigurationPropertiesBindingPostProcessor bindingPostProcessor) {
        this.bindingService = bindingService;
        this.bindingPostProcessor = bindingPostProcessor;
    }

    /**
     * Enum defining the types of binding channels in Spring Cloud Stream.
     */
    @Getter
    @AllArgsConstructor
    public enum BindingChannel {

        IN("in", Arrays.asList(Consumer.class, Function.class)),
        OUT("out", Arrays.asList(Supplier.class, Function.class));

        private final String channelType;
        private final List<Class<?>> capableFunctionList;

        public boolean isCapableFunction(Class<?> clazz) {
            return this.getCapableFunctionList().contains(clazz);
        }

        public String getChannelName(String functionName, int channelNumber) {
            return StringUtils.joinWith("-", functionName, this.getChannelType(), channelNumber);
        }

        public String getChannelPropertyPath(String channelName) {
            String prefix = "spring.cloud.function.bindings";
            return StringUtils.joinWith(".", prefix, channelName);
        }
    }

    /**
     * Restores the BindingService state to the initial values from application-test.yml.
     * <p>
     * Since the destination may have been modified before or after the test execution,
     * this method resets it to its original state.
     * </p>
     */
    public void initializeBindingService() {

        log.info("Resetting BindingService Bean to its initial creation state");
        this.unbindAll();
        this.bindAllByDefaultProperty();
    }

    public void unbindAll() {

        log.debug("Unbinding all existing bindings\nproducers: {}\nconsumers: {}",
                bindingService.getProducerBindingNames(), bindingService.getConsumerBindingNames());

        for (String bindingName : bindingService.getProducerBindingNames()) {
            bindingService.unbindProducers(bindingName);
        }

        for (String bindingName : bindingService.getConsumerBindingNames()) {
            bindingService.unbindConsumers(bindingName);
        }
    }

    public void bindAllByDefaultProperty() {

        BindingServiceProperties bindingServiceProperties = bindingService.getBindingServiceProperties();
        bindingPostProcessor.postProcessBeforeInitialization(bindingServiceProperties, "bindingServiceProperties");
        log.debug("Setting default property values into bindingServiceProperties");

        Map<String, BindingProperties> bindings = bindingServiceProperties.getBindings();
        log.debug("Binding all predefined bindings: {}", bindings.keySet());

        for (String bindingName : bindings.keySet()) {
            if (StringUtils.contains(bindingName, BindingChannel.OUT.getChannelType())) {
                bindingService.bindProducer(new DirectChannel(), bindingName);
            } else if (StringUtils.contains(bindingName, BindingChannel.IN.getChannelType())) {
                bindingService.bindConsumer(new DirectChannel(), bindingName);
            }
        }
    }

    public Binding<?> changeProducerFunctionBindingsDestination(String channelName, String destination) {

        log.debug("Changing ProducerBinding({}) destination to {}", channelName, destination);

        if (bindingService.getProducerBinding(channelName) != null) {
            log.debug("Releasing existing ProducerBinding\nchannel: {}\nProducerBinding: {}", channelName, bindingService.getProducerBinding(channelName));
            // Unbind existing producer binding
            bindingService.unbindProducers(channelName);
        }

        // Update existing binding information with new destination
        BindingServiceProperties bindingServiceProperties = bindingService.getBindingServiceProperties();
        bindingServiceProperties.getBindings().get(channelName).setDestination(destination);

        // Create a new MessageChannel and apply the new binding
        return bindingService.bindProducer(new DirectChannel(), channelName);
    }

    public Binding<?> changeProducerFunctionBindingsDestination(String functionName, BindingChannel channel, int channelNumber, String destination) {

        String channelName = Arrays.stream(bindingService.getProducerBindingNames())
                .filter(bindingName -> StringUtils.equals(bindingName, channel.getChannelName(functionName, channelNumber)))
                .findAny().orElseThrow(() -> new RuntimeException("Invalid channel"));

        return this.changeProducerFunctionBindingsDestination(channelName, destination);
    }

    public Collection<Binding<MessageChannel>> changeConsumerFunctionBindingsDestination(String channelName, String destination) {

        log.debug("Changing ConsumerBinding({}) destination to {}", channelName, destination);

        if (bindingService.getConsumerBindings(channelName) != null) {
            log.debug("Releasing existing ConsumerBinding\nchannel: {}\nConsumerBinding: {}", channelName, bindingService.getConsumerBindings(channelName));
            // Unbind existing consumer binding
            bindingService.unbindConsumers(channelName);
        }

        // Update existing binding information with new destination
        BindingServiceProperties bindingServiceProperties = bindingService.getBindingServiceProperties();
        bindingServiceProperties.getBindings().get(channelName).setDestination(destination);

        // Create a new MessageChannel and apply the new binding
        return bindingService.bindConsumer(new DirectChannel(), channelName);
    }

    public Collection<Binding<MessageChannel>> changeConsumerFunctionBindingsDestination(String functionName, BindingChannel channel, int channelNumber, String destination) {

        String channelName = Arrays.stream(bindingService.getConsumerBindingNames())
                .filter(bindingName -> StringUtils.equals(bindingName, channel.getChannelName(functionName, channelNumber)))
                .findAny().orElseThrow(() -> new RuntimeException("Invalid channel"));

        return this.changeConsumerFunctionBindingsDestination(channelName, destination);
    }
}
