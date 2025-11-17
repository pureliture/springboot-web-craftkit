package com.springboot.craftkit.framework.scope;

import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExposedCustomAttributeNamesConfigDataTest {

    @Test
    void bindsFromPropertiesAndConfigData() {
        var context = new SpringApplicationBuilder(TestConfig.class)
                .properties(
                        "sf.custom-attributes.global.exposed-headers.set[0]=X-Trace-Id",
                        "sf.custom-attributes.exposed-headers.add[0]=X-User-Id"
                )
                .web(WebApplicationType.NONE)
                .run();
        try (context) {
            // Ensure initialization from the Environment (mimic runtime initializer)
            ExposedCustomAttributeNames.initializeFromEnvironment(context.getEnvironment());
            assertTrue(ExposedCustomAttributeNames.isContains("X-Trace-Id"));
            assertTrue(ExposedCustomAttributeNames.isContains("X-User-Id"));
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfig { }
}
