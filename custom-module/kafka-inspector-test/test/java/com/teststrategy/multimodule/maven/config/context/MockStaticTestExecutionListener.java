package com.teststrategy.multimodule.maven.config.context;

import com.teststrategy.multimodule.maven.sf.framework.util.PropertyUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.mockito.BDDMockito;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Scanner;


/**
 * {@code MockStaticTestExecutionListener} is a custom test execution listener
 * that mocks static method calls and provides predefined values based on active profiles.
 *
 * <p>
 * This listener ensures that tests relying on static utility methods, such as
 * {@link PropertyUtil}, receive controlled responses. It dynamically loads mock data
 * from profile-specific YAML configuration files and registers them for test execution.
 * </p>
 *
 * <h2>Core Responsibilities:</h2>
 * <ul>
 *     <li>Intercepts static method calls to {@link PropertyUtil#getLastActiveProfile()} and provides a mocked response.</li>
 *     <li>Loads resource paths dynamically based on the active profile.</li>
 *     <li>Provides mock resources for URL-based configurations.</li>
 *     <li>Ensures that static mocks are properly initialized before tests and cleaned up after.</li>
 * </ul>
 *
 * <h2>Profile-Specific Behavior:</h2>
 * <ul>
 *     <li>The active profile is determined using <code>-Dspring.profiles.active</code> or defaults to <code>test</code>.</li>
 *     <li>Configuration files such as <code>application-test.yml</code> are dynamically loaded.</li>
 *     <li>Mocked resources are registered based on profile-specific properties.</li>
 * </ul>
 */
public class MockStaticTestExecutionListener extends AbstractTestExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(MockStaticTestExecutionListener.class);

    private static MockedStatic<PropertyUtil> mockedPropertyUtil;
    private String testYamlName;
    private String activeProfile;

    @Override
    public void beforeTestClass(TestContext testContext) {
        log.info("Initializing static mock before test class execution.");

        activeProfile = System.getProperty("spring.profiles.active", "test");
        testYamlName = StringUtils.join("application", "-", activeProfile, ".yml");

        this.initializeMockStatic();
    }

    @Override
    public void afterTestClass(TestContext testContext) {
        log.info("Cleaning up static mock after test class execution.");
        this.cleanupMockStatic();
    }

    private void initializeMockStatic() {
        // Clear existing inline mocks to avoid conflicts.
        Mockito.framework().clearInlineMocks();
        log.debug("Cleared existing static mocks.");

        // Mock PropertyUtil static methods.
        mockedPropertyUtil = Mockito.mockStatic(PropertyUtil.class);
        mockedPropertyUtil.when(PropertyUtil::getLastActiveProfile).thenReturn(activeProfile);

        // Load topic-related resource URLs dynamically from the profile-specific YAML.
        String publishResourceUrl = getTargetUrlFromProperty(testYamlName,
                "sample-framework.custom-module.inspector.topic.publish.resource-url");

        String subscribeResourceUrl = getTargetUrlFromProperty(testYamlName,
                "sample-framework.custom-module.inspector.topic.subscribe.resource-url");

        // Register mock resources based on loaded URLs.
        registerMockResourceForTargetUrl(publishResourceUrl, loadMockDataFromFile(getMockResponseFilePath(publishResourceUrl)));
        registerMockResourceForTargetUrl(subscribeResourceUrl, loadMockDataFromFile(getMockResponseFilePath(subscribeResourceUrl)));
    }

    private void cleanupMockStatic() {
        if (mockedPropertyUtil != null) {
            mockedPropertyUtil.close();
            mockedPropertyUtil = null;
        }

        // Clear any remaining inline mocks to ensure a clean test state.
        Mockito.framework().clearInlineMocks();
    }

    private String getMockResponseFilePath(String resourceUrl) {
        return "mock/" + resourceUrl;
    }

    private String getTargetUrlFromProperty(String ymlPath, String propertyName) {
        String resourceUrl = getPropertyValue(ymlPath, propertyName);
        String applicationName = getPropertyValue(ymlPath, "spring.application.name");

        return resourceUrl.replace("{application-name}", applicationName)
                .replace("{profile}", PropertyUtil.getLastActiveProfile());
    }

    private String getPropertyValue(String ymlPath, String propertyName) {
        YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
        factoryBean.setResources(new ClassPathResource(ymlPath));

        Properties properties = factoryBean.getObject();
        return Optional.ofNullable(properties).map(p -> p.getProperty(propertyName)).orElse(null);
    }

    @SneakyThrows
    private void registerMockResourceForTargetUrl(String targetUrl, String intendedResponse) {
        log.debug("Registering mock resource for URL: {}\nMock response: {}", targetUrl, intendedResponse);
        Resource mockResource = Mockito.mock(Resource.class);

        InputStream fakeInputStream = new ByteArrayInputStream(intendedResponse.getBytes());
        BDDMockito.given(mockResource.getInputStream()).willReturn(fakeInputStream);

        mockedPropertyUtil.when(() -> PropertyUtil.getResource(targetUrl)).thenReturn(mockResource);
        log.debug("Successfully registered mock resource for URL: {}", targetUrl);
    }

    private String loadMockDataFromFile(String resourcePath) {
        try {
            Resource resource = new ClassPathResource(resourcePath);
            InputStream inputStream = resource.getInputStream();
            Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } catch (IOException e) {
            log.error("Error occurred while reading the resource file: {}", resourcePath, e);
            throw new RuntimeException("Failed to load mock data: " + resourcePath, e);
        }
    }
}
