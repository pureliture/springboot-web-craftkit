package com.teststrategy.multimodule.maven.sf.framework.util;

import com.teststrategy.multimodule.maven.sf.framework.application.ApplicationContextUtil;
import com.teststrategy.multimodule.maven.sf.framework.resource.RetryableUrlResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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


@Slf4j
public class PropertyUtil {

    private static final String DELIMITER = ",";
    private static String applicationName = "";
    private static List<String> profiles;

    public static boolean hasAnyProfileByEndWith(String... args) {
        try {
            if (CollectionUtils.isEmpty(profiles)) {
                Environment environment = ApplicationContextUtil.getApplicationContext().getEnvironment();
                String[] localProfiles = environment.getActiveProfiles();

                if (localProfiles.length == 0) {
                    localProfiles = environment.getDefaultProfiles();
                }

                profiles = Arrays.asList(localProfiles);
            }

            if (CollectionUtils.isEmpty(profiles))
                return false;

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
