package com.teststrategy.multimodule.maven.sf.framework.util;

import com.teststrategy.multimodule.maven.sf.framework.application.ApplicationContextUtil;
import com.teststrategy.multimodule.maven.sf.framework.resource.RetryableUrlResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Slf4j
public class PropertyUtil {

    private static final String DELIMITER = ",";
    private static String applicationName = "";
    private static List<String> profiles;


    /**
     * Environment에서 Active Profiles를 가져와 profiles 리스트를 초기화한다.
     */
    private static void initializeProfiles() {

        Environment environment = ApplicationContextUtil.getApplicationContext().getEnvironment();

        profiles = List.of(ArrayUtils.isEmpty(environment.getActiveProfiles()) ?
                environment.getDefaultProfiles() : environment.getActiveProfiles());
    }

    public static String getLastActiveProfile() {
        try {
            if (CollectionUtils.isEmpty(profiles)) {
                initializeProfiles();
            }
            return profiles.get(profiles.size() - 1);
        } catch (Exception e) {
            log.error("Failed to get last active profile", e);
            return StringUtils.EMPTY;
        }
    }


    public static boolean hasAnyProfileByEndWith(String... args) {
        try {
            if (CollectionUtils.isEmpty(profiles)) {
                initializeProfiles();
            }

            if (CollectionUtils.isEmpty(profiles)) return false;

            return profiles.stream()
                    .anyMatch(profile -> Arrays.stream(args).anyMatch(arg ->
                            StringUtils.endsWithIgnoreCase(profile, arg)));
        } catch (Exception e) {
            return false;
        }
    }

    public static Resource getResource(String resourcePath) {
        return getResourceOrRetryOnHttp(resourcePath);
    }

    /**
     * 디렉토리경로, file:, classpath:, http:, jar: 형식의 리소스를 생성한다.
     * Http Resource 인 경우 retry 를  수행한다.
     *
     * @param resourcePath 리소스경로
     * @return resource  resource.exist() 를 체크하지 않으므로, 리소스가 없다면, getInputStream()으로 리소스 오픈시 오류가 발생한다.
     */
    private static Resource getResourceOrRetryOnHttp(String resourcePath) {

        if(StringUtils.startsWithAny(resourcePath, new String[] {ResourceUtils.CLASSPATH_URL_PREFIX, ResourceUtils.FILE_URL_PREFIX})) {
            return new PathMatchingResourcePatternResolver().getResource(resourcePath);
        } else if(StringUtils.startsWithAny(resourcePath, new String[]{ResourceUtils.JAR_URL_PREFIX})) {
            try {
                return new UrlResource(resourcePath);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        } else if(StringUtils.startsWithAny(resourcePath, new String[]{"http:", "https:"})) {
            try {
                return new RetryableUrlResource(resourcePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            Resource resource = new PathMatchingResourcePatternResolver().getResource(resourcePath);
            // schema 없이 class path
            // ex. config/config.properties or /config/config.properties
            if(resource.exists()) {
                return resource;
            } else {
                // absolute file path 인지 다시 한번 확인 한다.
                Path filePath = Paths.get(resourcePath).toAbsolutePath().normalize();
                try {
                    return new UrlResource(filePath.toUri());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
