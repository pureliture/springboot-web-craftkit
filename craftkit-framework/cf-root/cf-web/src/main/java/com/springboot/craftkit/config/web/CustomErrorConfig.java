package com.springboot.craftkit.config.web;

import com.springboot.craftkit.framework.response.CraftkitErrorAttributes;
import com.springboot.craftkit.framework.response.CraftkitErrorController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;

import java.util.stream.Collectors;

/**
 * Custom error configuration for Craftkit framework.
 * Configures custom error attributes and error controller.
 */
@AutoConfiguration
@Slf4j
@AutoConfigureBefore(ErrorMvcAutoConfiguration.class)
@ConditionalOnWebApplication
public class CustomErrorConfig {

    private final ServerProperties serverProperties;

    public CustomErrorConfig(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    @Bean
    public CraftkitErrorAttributes craftkitErrorAttributes() {
        log.info("CraftkitErrorAttributes@CustomErrorConfig was created as Bean!");
        return new CraftkitErrorAttributes();
    }

    @Bean
    public CraftkitErrorController basicErrorController(ErrorAttributes errorAttributes,
            ObjectProvider<ErrorViewResolver> errorViewResolvers) {
        return new CraftkitErrorController(errorAttributes, this.serverProperties.getError(),
                errorViewResolvers.orderedStream().collect(Collectors.toList()));
    }

}
