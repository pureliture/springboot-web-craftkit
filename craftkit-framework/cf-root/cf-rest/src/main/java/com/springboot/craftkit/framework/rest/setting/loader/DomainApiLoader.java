package com.springboot.craftkit.framework.rest.setting.loader;

import com.springboot.craftkit.framework.rest.setting.DomainApiProperties;
import org.springframework.core.io.ResourceLoader;

import java.util.LinkedHashMap;

/**
 * Abstraction for loading Domain API map from a configuration resource.
 *
 * Returns a LinkedHashMap to preserve declaration order.
 */
public interface DomainApiLoader {

    /**
     * Load domain -> (api -> ApiProperties) map from the given path.
     *
     * @param resourceLoader Spring ResourceLoader to resolve the path
     * @param path resource location (e.g., classpath:config/domain-api.yml or file:/...)
     * @return map or empty map if not found
     * @throws Exception when the resource cannot be parsed
     */
    LinkedHashMap<String, LinkedHashMap<String, DomainApiProperties.ApiProperties>> load(ResourceLoader resourceLoader, String path) throws Exception;

    /**
     * A short loader id used for logs (e.g., "simple" or "bind").
     */
    String id();
}
