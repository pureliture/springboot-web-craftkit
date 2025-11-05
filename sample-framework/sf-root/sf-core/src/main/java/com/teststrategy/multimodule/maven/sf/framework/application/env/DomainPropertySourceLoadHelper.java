package com.teststrategy.multimodule.maven.sf.framework.application.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Loads a domain.yml into Spring Environment if configured.
 *
 * The path is read from property key: {@code sample-framework.rest.domain.config}
 * - If the property points to a directory, this helper tries to load {@code domain.yml} from that directory.
 * - If the property points directly to a YAML file, that file is loaded.
 * - If the property is not set or the resource doesn't exist, this helper does nothing.
 */
public class DomainPropertySourceLoadHelper {

    private static final Logger log = LoggerFactory.getLogger(DomainPropertySourceLoadHelper.class);

    public static final String PROPERTY_KEY = "sample-framework.rest.domain.config";

    private final ConfigurableEnvironment environment;
    private final ResourceLoader resourceLoader;

    public DomainPropertySourceLoadHelper(ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
        this.environment = environment;
        this.resourceLoader = resourceLoader;
    }

    public void load() {
        String path = environment.getProperty(PROPERTY_KEY);
        if (path == null || path.isBlank()) {
            if (log.isDebugEnabled()) {
                log.debug("{} not set. Skipping domain.yml load.", PROPERTY_KEY);
            }
            return;
        }

        Resource resource = resolveResource(path);
        if (!resource.exists()) {
            // If path is a dir, try domain.yml inside it
            File f = new File(path);
            if (f.isDirectory()) {
                File candidate = new File(f, "domain.yml");
                resource = new FileSystemResource(candidate);
            }
        }

        if (!resource.exists()) {
            log.warn("domain.yml resource not found for path: {}", path);
            return;
        }

        try {
            YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
            List<PropertySource<?>> propertySources = loader.load("domain", resource);
            MutablePropertySources envSources = environment.getPropertySources();
            for (PropertySource<?> ps : propertySources) {
                // avoid duplicate property source names
                if (envSources.contains(ps.getName())) {
                    if (log.isDebugEnabled()) {
                        log.debug("PropertySource '{}' already exists. Skipping.", ps.getName());
                    }
                    continue;
                }
                // add last to avoid overriding existing application properties unless explicitly desired
                envSources.addLast(ps);
                if (log.isInfoEnabled()) {
                    log.info("Loaded domain properties from {} as PropertySource '{}'.", resource, ps.getName());
                }
            }
        } catch (IOException e) {
            log.warn("Failed to load domain.yml from {}", resource, e);
        }
    }

    private Resource resolveResource(String path) {
        try {
            // First try as file system path
            File file = new File(path);
            if (file.exists()) {
                return new FileSystemResource(file);
            }
        } catch (Exception ignored) {}

        // Try via ResourceLoader (may support classpath: or other prefixes)
        try {
            Resource res = resourceLoader.getResource(path);
            if (res != null && res.exists()) {
                return res;
            }
        } catch (Exception ignored) {}

        // Fallback: classpath relative
        return new ClassPathResource(path);
    }
}
