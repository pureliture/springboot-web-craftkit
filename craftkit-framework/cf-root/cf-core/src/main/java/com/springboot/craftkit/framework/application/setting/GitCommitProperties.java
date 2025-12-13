package com.springboot.craftkit.framework.application.setting;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.info.GitProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.*;

/**
 * Git commit properties collector for build information.
 * Collects git.properties from classpath resources and provides build info.
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class GitCommitProperties
        extends TreeMap<String, GitCommitProperties.BuildProperties>
        implements Map<String, GitCommitProperties.BuildProperties> {

    private static final long serialVersionUID = 2960033477263029140L;

    @JsonIgnore
    private transient GitProperties gitProperties = null;

    public GitCommitProperties() {

        super(Comparator.comparing(String::toString));

        Resource[] resources = getResources();
        if (ArrayUtils.isEmpty(resources))
            return;

        // GitProperties
        setGitProperties(resources[0]);

        // main module
        setProperties(true, resources[0]);

        // jar
        Resource[] submoduleResources = (Resource[]) ArrayUtils.subarray(resources, 1, resources.length);
        setProperties(false, submoduleResources);

        log.info("Build information :: \n{}", this);
    }

    private void setGitProperties(Resource resource) {
        Properties properties = getProperties(resource);
        Properties newProperties = new Properties();
        if (properties != null) {
            properties.forEach((key, value) -> {
                String newKey = key.toString().replaceAll("^.*\\.git\\.", "");
                newProperties.setProperty(newKey, (String) value);
            });
        }

        gitProperties = new GitProperties(newProperties);
    }

    private void setProperties(boolean isMain, Resource... resources) {
        Properties properties = getProperties(resources);
        if (properties == null)
            return;

        properties.forEach((key1, value1) -> {

            String key = (String) key1;
            String value = (String) value1;
            String[] levels = key.split("\\.");

            String moduleName = "default";
            String propertyName = key;

            // build.${project.artifactId}.git
            if (StringUtils.equalsIgnoreCase(levels[0], "build")) {
                moduleName = levels[1];
                propertyName = getPropertyName(levels, 2);
            }
            BuildProperties module = getBuildProperties(moduleName, isMain);

            module.put(propertyName, value);
        });
    }

    private Resource[] getResources() {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(GitCommitProperties.class.getClassLoader());
        Resource[] resources;
        try {
            resources = resolver.getResources("classpath*:/**/*git.properties");
        } catch (IOException e) {
            return new Resource[0];
        }

        if (ArrayUtils.isEmpty(resources))
            return new Resource[0];

        return resources;
    }

    private Properties getProperties(Resource... resources) {
        try {
            PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
            propertiesFactoryBean.setLocations(resources);
            propertiesFactoryBean.afterPropertiesSet();
            return propertiesFactoryBean.getObject();
        } catch (IOException e) {
            return null;
        }

    }

    private BuildProperties getBuildProperties(String moduleName, boolean isMain) {
        return this.computeIfAbsent(moduleName,
                key -> new BuildProperties(isMain, StringUtils.startsWith(key, "cf-")));
    }

    private String getPropertyName(String[] levels, int i) {
        StringBuilder name = new StringBuilder();
        name.append(levels[i]);
        for (i++; i < levels.length; i++) {
            name.append(".").append(levels[i]);
        }
        return name.toString();
    }

    @EqualsAndHashCode(callSuper = true)
    public static class BuildProperties
            extends LinkedHashMap<String, String>
            implements Map<String, String> {

        private static final long serialVersionUID = -6362591743680579744L;

        private final boolean main;
        private final boolean craftkit;

        public BuildProperties(boolean isMain, boolean isCraftkit) {
            super();
            this.main = isMain;
            this.craftkit = isCraftkit;
        }

        public boolean isMain() {
            return main;
        }

        public boolean isCraftkit() {
            return craftkit;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            this.forEach((key, value) -> builder.append("\t")
                    .append(key)
                    .append(" : ")
                    .append(value)
                    .append("\n"));
            return builder.toString();
        }

        private static final String[][] brifKeys = {
                {"git.branch", "%-7.7s", "Branch"},
                {"git.build.version", "%-15.15s", "Build Version"},
                {"git.build.time", "%-19.19s", "Build Time"},
                {"git.commit.id.abbrev", "%-7.7s", "Commit"},
                {"git.commit.time", "%-19.19s", "Commit Time"}
        };

        public static String toBrifHeader() {
            StringBuilder builder = new StringBuilder();
            Arrays.asList(brifKeys)
                    .forEach(keyArray -> builder.append(String.format(keyArray[1], keyArray[2])).append(" | "));

            return builder.toString();
        }

        public String toBrifString() {
            StringBuilder builder = new StringBuilder();
            Arrays.asList(brifKeys).forEach(keyArray -> {
                String value = StringUtils.defaultString(this.get(keyArray[0]));
                builder.append(String.format(keyArray[1], value)).append(" | ");
            });
            return builder.toString();
        }
    }

    /**
     * @return the gitProperties
     */
    public GitProperties getGitProperties() {
        return gitProperties;
    }


    @Override
    public String toString() {
        String moudleFormat = "%-23.23s";
        StringBuilder builder = new StringBuilder();

        builder.append(String.format(moudleFormat, "Module"))
                .append(" | ")
                .append(BuildProperties.toBrifHeader())
                .append("\n");

        this.forEach((module, buildInfo) -> builder.append(String.format(moudleFormat, module))
                .append(" | ")
                .append(buildInfo.toBrifString())
                .append("\n"));

        return builder.toString();
    }

}
