package com.teststrategy.multimodule.maven.sf.framework.properties;

import com.teststrategy.multimodule.maven.sf.framework.resource.RetryableUrlResource;
import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.core.CollectionFactory;
import org.springframework.core.io.Resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class YamlPropertiesProcessor extends YamlProcessor {

    public YamlPropertiesProcessor(Resource resource) throws IOException {
        if (!(resource instanceof RetryableUrlResource) && !resource.exists()) {
            throw new FileNotFoundException();
        }

        this.setResources(resource);
    }

    public Properties createProperties() {
        final Properties result = CollectionFactory.createStringAdaptingProperties();
        process((properties, map) -> result.putAll(properties));
        return result;
    }
}
