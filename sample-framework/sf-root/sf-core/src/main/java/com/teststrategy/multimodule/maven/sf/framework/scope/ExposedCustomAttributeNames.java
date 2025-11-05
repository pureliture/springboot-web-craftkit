package com.teststrategy.multimodule.maven.sf.framework.scope;

import org.apache.commons.collections.SetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.*;

public class ExposedCustomAttributeNames {

    private static final Logger log = LoggerFactory.getLogger(ExposedCustomAttributeNames.class);

    // Neutral, simplified prefix for this library
    private static final String PROPERTY_PREFIX = "sf.custom-attributes";
    private static final String PROPERTY_NAME = PROPERTY_PREFIX + ".exposed-headers";

    private static Set<String> attributes = SetUtils.synchronizedSet(new TreeSet<>(String.CASE_INSENSITIVE_ORDER));
    static {
        attributes.addAll(List.of());
    }

    private static final Set<String> additionalAttributes = SetUtils.synchronizedSet(new TreeSet<>(String.CASE_INSENSITIVE_ORDER));

    /**
     * Initialize exposed custom attributes from the current Environment using Spring's Binder.
     * This method relies solely on ConfigData-loaded PropertySources and does not inject new sources.
     */
    public static void initializeFromEnvironment(ConfigurableEnvironment environment) {
        Binder binder = Binder.get(environment);
        setAttributeByEnv(binder);
        log.debug("ExposedCustomAttributeNames initialized: size={} (+{})", attributes.size(), additionalAttributes.size());
    }

    private static void setAttributeByEnv(Binder binder) {
        List<String> sets = binder.bind(PROPERTY_PREFIX + ".global.exposed-headers.set", Bindable.listOf(String.class)).orElse(Collections.emptyList());
        List<String> adds = binder.bind(PROPERTY_PREFIX + ".global.exposed-headers.add", Bindable.listOf(String.class)).orElse(new ArrayList<>());
        List<String> sets1 = binder.bind(PROPERTY_NAME + ".set", Bindable.listOf(String.class)).orElse(Collections.emptyList());
        List<String> adds1 = binder.bind(PROPERTY_NAME + ".add", Bindable.listOf(String.class)).orElse(Collections.emptyList());

        if (!sets1.isEmpty()) {
            sets = sets1;
        }
        adds.addAll(adds1);

        if (!sets.isEmpty()) {
            setAttributes(sets);
        }
        if (!adds.isEmpty()) {
            setAdditionalAttributes(adds);
        }
    }

    protected static void setAttributes(List<String> newAttributes) {
        if (newAttributes == null) {
            newAttributes = new ArrayList<>();
        }
        Set<String> newSets = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        newSets.addAll(newAttributes);
        attributes = SetUtils.synchronizedSet(newSets);
    }

    protected static void setAdditionalAttributes(List<String> newAdditionalAttributes) {
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
