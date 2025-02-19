package com.teststrategy.multimodule.maven.config;


import com.teststrategy.multimodule.maven.config.properties.TopicInspectorProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.stream.binding.BindingService;
import org.springframework.context.annotation.Bean;



@ConditionalOnClass({BindingService.class, TopicInspectorConfig.class})
@ConditionalOnProperty(prefix = TopicInspectorProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@TestConfiguration
public class TopicInspectorTestPresetConfig {

    @Bean
    public SpringCloudStreamBindModifier springCloudStreamBindModifier(BindingService bindingService,
                                                                       ConfigurationPropertiesBindingPostProcessor bindingPostProcessor) {
        return new SpringCloudStreamBindModifier(bindingService, bindingPostProcessor);
    }
}
