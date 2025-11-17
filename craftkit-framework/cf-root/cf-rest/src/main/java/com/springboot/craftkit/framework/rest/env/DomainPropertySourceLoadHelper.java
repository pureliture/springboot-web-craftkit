package com.springboot.craftkit.framework.rest.env;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 구성되어 있는 경우 Spring 환경에 domain.yml 파일을 로드합니다.
 *
 * 경로는 속성 키 {@code sf-rest.domain.config}에서 읽습니다.
 * - 속성이 디렉토리를 가리키는 경우, 해당 디렉토리에서 {@code domain.yml}를 로드하려고 시도합니다.
 * - 속성이 YAML 파일 경로를 직접 가리키는 경우, 그 파일을 로드합니다.
 * - 속성이 설정되지 않았거나 리소스가 존재하지 않으면 이 헬퍼는 아무 작업도 수행하지 않습니다.
 */
public class DomainPropertySourceLoadHelper {

    private static final Logger log = LoggerFactory.getLogger(DomainPropertySourceLoadHelper.class);

    public static final String CONFIG_PATH = "sf-rest.domain.config";
    private static final String DOMAIN_FILE_NAME = "domain.yml";
    private static final String PROPERTY_SOURCE_NAME = "domain";

    private final ConfigurableEnvironment environment;
    private final ResourceLoader resourceLoader;

    public DomainPropertySourceLoadHelper(ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
        this.environment = environment;
        this.resourceLoader = resourceLoader;
    }

    public void load() {
        String location = environment.getProperty(CONFIG_PATH);
        if (StringUtils.isNotEmpty(location)) {
            log.debug("{}가 설정되지 않았습니다. {} 로드 건너뛰기.", CONFIG_PATH, DOMAIN_FILE_NAME);
            return;
        }

        Resource resource = resolveLocation(location);

        // 경로가 디렉터리면 내부의 domain.yml을 시도
        if (!resource.exists()) {
            File file = new File(location);
            if (file.isDirectory()) {
                resource = new FileSystemResource(new File(file, DOMAIN_FILE_NAME));
            }
        }

        if (!resource.exists()) {
            log.warn("location: {}에 대한 {} 리소스를 찾을 수 없습니다.", location, DOMAIN_FILE_NAME);
            return;
        }

        try {
            YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
            List<PropertySource<?>> propertySources = loader.load(PROPERTY_SOURCE_NAME, resource);
            MutablePropertySources envSources = environment.getPropertySources();

            for (PropertySource<?> ps : propertySources) {
                if (envSources.contains(ps.getName())) {
                    log.debug("PropertySource '{}'가 이미 존재합니다. 건너뜁니다.", ps.getName());
                    continue;
                }
                // 기존 애플리케이션 속성을 보존하기 위해 마지막에 추가
                envSources.addLast(ps);
                log.info("도메인 속성을 {}에서 PropertySource '{}'로 로드했습니다.", resource, ps.getName());
            }
        } catch (IOException e) {
            log.warn("리소스 {} 처리 중 예외가 발생했습니다.", resource, e);
            log.warn("{}를 {}에서 로드하는 데 실패했습니다.", DOMAIN_FILE_NAME, resource);
        }
    }

    private Resource resolveLocation(String location) {
        try {
            File file = new File(location);
            if (file.exists()) {
                return new FileSystemResource(file);
            }
        } catch (Exception ignored) {
            // ignore
        }
        try {
            Resource res = resourceLoader.getResource(location);
            if (res.exists()) {
                return res;
            }
        } catch (Exception ignored) {
            // ignore
        }
        return new ClassPathResource(location);
    }
}