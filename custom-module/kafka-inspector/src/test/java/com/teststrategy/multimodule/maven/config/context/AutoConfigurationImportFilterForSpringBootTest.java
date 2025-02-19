package com.teststrategy.multimodule.maven.config.context;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;

import java.util.Arrays;
import java.util.List;

/**
 * {@code AutoConfigurationImportFilterForSpringBootTest} is a custom filter that
 * excludes specific auto-configuration classes during Spring Boot Test execution.
 *
 * <p>
 * This filter ensures that unwanted auto-configurations, such as those from external
 * dependencies, are not loaded when running tests. It enhances test performance and
 * prevents conflicts with unnecessary configurations.
 * </p>
 *
 * <h2>Filtering Behavior:</h2>
 * <ul>
 *     <li>Excludes all auto-configurations that belong to specific package prefixes.</li>
 *     <li>Prevents loading of unnecessary configurations during Spring Boot Test execution.</li>
 *     <li>By default, excludes classes under the <code>io.eventuate</code> package.</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <p>
 * This filter is automatically picked up by Spring Boot's auto-configuration mechanism
 * when included in the test classpath.
 * </p>
 */
public class AutoConfigurationImportFilterForSpringBootTest implements AutoConfigurationImportFilter {

    private static final List<String> EXCLUDED_PACKAGES = Arrays.asList("io.eventuate");

    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
        boolean[] matchResults = new boolean[autoConfigurationClasses.length];

        for (int i = 0; i < autoConfigurationClasses.length; i++) {
            String className = autoConfigurationClasses[i];

            if (className == null) {
                matchResults[i] = true; // Include null values
            } else if (EXCLUDED_PACKAGES.stream().anyMatch(className::startsWith)) {
                matchResults[i] = false; // Exclude matching packages
            } else {
                matchResults[i] = true; // Include other configurations
            }
        }
        return matchResults;
    }
}