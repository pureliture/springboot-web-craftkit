package com.teststrategy.multimodule.maven.sf.framework.rest.setting.loader;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.teststrategy.multimodule.maven.sf.framework.rest.setting.DomainApiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedHashMap;

/**
 * Jackson ObjectMapper + YAMLFactory based loader.
 */
@Slf4j
public class YamlObjectMapperDomainApiLoader implements DomainApiLoader {

    @Override
    public LinkedHashMap<String, LinkedHashMap<String, DomainApiProperties.ApiProperties>> load(ResourceLoader resourceLoader, String path) throws Exception {
        Resource resource = resolveResource(resourceLoader, path);
        if (!resource.exists()) {
            log.warn("DomainApi YAML not found. path={}", path);
            return new LinkedHashMap<>();
        }
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream is = resource.getInputStream()) {
            var tf = mapper.getTypeFactory();
            JavaType stringType = tf.constructType(String.class);
            JavaType inner = tf.constructMapType(LinkedHashMap.class, stringType, tf.constructType(DomainApiProperties.ApiProperties.class));
            JavaType rootType = tf.constructMapType(LinkedHashMap.class, stringType, inner);
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, LinkedHashMap<String, DomainApiProperties.ApiProperties>> map =
                    mapper.readValue(is, rootType);
            return map != null ? map : new LinkedHashMap<>();
        }
    }

    private Resource resolveResource(ResourceLoader resourceLoader, String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                return new FileSystemResource(file);
            }
        } catch (Exception ignored) {
        }
        try {
            Resource res = resourceLoader != null ? resourceLoader.getResource(path) : null;
            if (res != null && res.exists()) {
                return res;
            }
        } catch (Exception ignored) {
        }
        return new ClassPathResource(path);
    }

    @Override
    public String id() {
        return "simple";
    }
}
