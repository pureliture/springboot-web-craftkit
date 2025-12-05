package com.springboot.craftkit.config.web;

import com.springboot.craftkit.framework.interceptor.AuditInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Web MVC configuration for Craftkit framework.
 */
@AutoConfiguration
@Slf4j
@EnableWebMvc
@ConditionalOnWebApplication
public class WebMvcConfig implements WebMvcConfigurer {

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS =
            {"classpath:/META-INF/resources/", "classpath:/resources/", "classpath:/static/", "classpath:/public/"};

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (!registry.hasMappingForPattern("/resources/**")) {
            registry.addResourceHandler("/resources/**")
                    .addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS)
                    .setCachePeriod(3600)
                    .resourceChain(true)
                    .addResolver(new EncodedResourceResolver())
                    .addResolver(new PathResourceResolver());
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // preHandle
        registry.addInterceptor(new AuditInterceptor());
        WebMvcConfigurer.super.addInterceptors(registry);
    }

}
