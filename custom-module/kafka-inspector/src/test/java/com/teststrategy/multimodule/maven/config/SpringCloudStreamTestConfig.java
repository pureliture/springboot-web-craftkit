package com.teststrategy.multimodule.maven.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.stream.binder.test.EnableTestBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * Configuration class for setting up a Spring Cloud Stream test environment.
 * <p>
 * This class uses {@link EnableTestBinder} to configure the Spring Cloud Stream binder
 * for execution in a test environment. This allows message bindings to be tested
 * without running an actual Kafka or other messaging middleware.
 * </p>
 *
 * <ul>
 *     <li>Activates a test binder using {@link EnableTestBinder}</li>
 *     <li>Enables automatic configuration of required settings via {@link EnableAutoConfiguration}</li>
 *     <li>Defines a {@link Supplier<String>} bean for sending messages</li>
 *     <li>Defines a {@link Consumer<String>} bean for receiving messages</li>
 * </ul>
 *
 * <p>
 * This configuration is executed within {@link org.springframework.boot.test.context.SpringBootTest}
 * and is used to test Spring Cloud Stream functionality.
 * </p>
 *
 * @author sanghyeok lee
 *
 * @see EnableTestBinder
 * @see org.springframework.boot.test.context.SpringBootTest
 */
@Profile("test")
@EnableTestBinder
@Configuration
@EnableAutoConfiguration(exclude = {EventuateInspectorConfig.class})
public class SpringCloudStreamTestConfig {

    @Bean
    public Supplier<String> publish() {
        return () -> "Published message";
    }

    @Bean
    public Consumer<String> subscribe() {
        return input -> {
            System.out.println("Received message: " + input);
        };
    }
}
