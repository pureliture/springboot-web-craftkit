package com.teststrategy.multimodule.maven.sf.framework.properties;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Slf4j
public class YamlPropertySourceFactory implements PropertySourceFactory {

    private static final String[] YML_FILE_EXTENSION = {".yml", ".yaml"};

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        String filename = resource.getResource().getFilename();

        if (filename != null
                && (filename.endsWith(YML_FILE_EXTENSION[0]) || filename.endsWith(YML_FILE_EXTENSION[1]))) {
            return name != null ? new YamlResourcePropertySource(name, resource)
                    : new YamlResourcePropertySource(getNameForResource(resource.getResource()), resource);
        }
        return (name != null ? new ResourcePropertySource(name, resource) : new ResourcePropertySource(resource));
    }

    private String getNameForResource(Resource resource) {
        String name = resource.getDescription();
        if (!StringUtils.hasText(name)) {
            name = resource.getClass().getSimpleName() + "@" + System.identityHashCode(resource);
        }
        return name;
    }
}
