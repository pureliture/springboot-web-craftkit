package com.teststrategy.multimodule.maven.sf.framework.scope;

import com.teststrategy.multimodule.maven.sf.framework.properties.YamlPropertiesProcessor;
import com.teststrategy.multimodule.maven.sf.framework.util.PropertyUtil;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ExposedCustomAttributeNames {

    private static final Logger log = LoggerFactory.getLogger(ExposedCustomAttributeNames.class);

    private static final String PROPERTY_PREFIX = "sample-framework..custom-attributes";
    private static final String PROPERTY_NAME = PROPERTY_PREFIX + ".exposed-headers";


    private static Set<String> attributes = SetUtils.synchronizedSet(new TreeSet<>(String.CASE_INSENSITIVE_ORDER));
    static {
        attributes.addAll(List.of());
    }

    private static final Set<String> additionalAttributes = SetUtils.synchronizedSet(new TreeSet<>(String.CASE_INSENSITIVE_ORDER));

    protected void loadByEnvironment(ConfigurableEnvironment environment) {

        loadByConfigIfExist(environment);

        Binder binder = Binder.get(environment);
        setAttributeByEnv(binder);
    }

    private void loadByConfigIfExist(ConfigurableEnvironment environment) {

        String configPrefix = PROPERTY_PREFIX +".config";
        String propertyPrefix = PROPERTY_PREFIX +".global.exposed-headers";
        String path = environment.getProperty(configPrefix);
        if(StringUtils.isBlank(path))
            return ;

        Resource resource = PropertyUtil.getResource(path);
        if (!resource.exists()) {
            throw new IllegalArgumentException(configPrefix+"(" + resource + ") does not exist");
        }

        Properties properties;
        try {
            properties = new YamlPropertiesProcessor(resource).createProperties();
        } catch (IOException e) {
            throw new IllegalArgumentException(configPrefix+"(" + resource + ") Yaml Processing error");
        }

        Enumeration<?> names = properties.propertyNames();
        Map<String,Object> mapByConfig = new ConcurrentHashMap<>();
        while(names.hasMoreElements()) {
            String name = (String) names.nextElement();

            String[] allowedPrefixs = new String[] {"global.exposed-headers.add","global.exposed-headers.set","exposed-headers.add","exposed-headers.set"};
            if(!StringUtils.startsWithAny(name, allowedPrefixs)) {
                log.warn("{}({}}) 속성 {}  허용되는 PREFIX{} 가 아니어서 무시합니다.", configPrefix, resource, name, Arrays.toString(allowedPrefixs));
                continue;
            }

            String newName = PROPERTY_PREFIX + (name.startsWith("global") ? "." : ".global.") + name;
            mapByConfig.put(newName, properties.get(name));
        }

        PropertySource<Map<String,Object>> propertySource = new MapPropertySource(propertyPrefix, mapByConfig);
        environment.getPropertySources().addFirst(propertySource);
    }

    private void setAttributeByEnv(Binder binder) {
        List<String> sets = binder.bind(PROPERTY_PREFIX +".global.exposed-headers.set", Bindable.listOf(String.class)).orElse(Collections.emptyList());
        List<String> adds = binder.bind(PROPERTY_PREFIX +".global.exposed-headers.add", Bindable.listOf(String.class)).orElse(new ArrayList<>());
        List<String> sets1 = binder.bind(PROPERTY_NAME +".set", Bindable.listOf(String.class)).orElse(Collections.emptyList());
        List<String> adds1 = binder.bind(PROPERTY_NAME +".add", Bindable.listOf(String.class)).orElse(Collections.emptyList());

        if(!sets1.isEmpty()) {
            sets = sets1;
        }

        adds.addAll(adds1);

        if(!sets.isEmpty()) {
            setAttributes(sets);
        }

        if(!adds.isEmpty()) {
            setAdditionalAttributes(adds);
        }
    }

    protected void setAttributes(List<String> newAttributes) {
        if (newAttributes == null) {
            newAttributes = new ArrayList<>();
        }
        Set<String> newSets = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        newSets.addAll(newAttributes);

        attributes = SetUtils.synchronizedSet(newSets);
    }

    protected void setAdditionalAttributes(List<String> newAdditionalAttributes) {
        if (newAdditionalAttributes == null) {
            return;
        }

        additionalAttributes.addAll(newAdditionalAttributes);
    }

    public static Set<String> getAttributes() {
        Set<String> allAttributes = new HashSet<>();
        allAttributes.addAll(attributes);
        allAttributes.addAll(additionalAttributes);
        return allAttributes;
    }

    public static boolean isContains(String name) {
        return attributes.contains(name) || additionalAttributes.contains(name);
    }

    public static boolean isNotContains(String name) {
        return !isContains(name);
    }
}
