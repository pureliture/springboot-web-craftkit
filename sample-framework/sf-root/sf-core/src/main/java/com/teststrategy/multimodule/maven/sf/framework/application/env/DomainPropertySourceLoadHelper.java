package com.teststrategy.multimodule.maven.sf.framework.application.env;

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

    public static final String PROPERTY_KEY = "sf-rest.domain.config";

    private final ConfigurableEnvironment environment;
    private final ResourceLoader resourceLoader;

    public DomainPropertySourceLoadHelper(ConfigurableEnvironment environment, ResourceLoader resourceLoader) {
        this.environment = environment;
        this.resourceLoader = resourceLoader;
    }

    public void load() {
        String path = environment.getProperty(PROPERTY_KEY);
        if (path == null || path.isBlank()) {
            if (log.isDebugEnabled()) {
                log.debug("{}가 설정되지 않았습니다. domain.yml 로드 건너뛰기.", PROPERTY_KEY);
            }
            return;
        }

        Resource resource = resolveResource(path);
        if (!resource.exists()) {
            // 경로가 디렉토리인 경우, 그 안의 domain.yml을 시도합니다.
            File f = new File(path);
            if (f.isDirectory()) {
                File candidate = new File(f, "domain.yml");
                resource = new FileSystemResource(candidate);
            }
        }

        if (!resource.exists()) {
            log.warn("path: {}에 대한 domain.yml 리소스를 찾을 수 없습니다.", path);
            return;
        }

        try {
            YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
            List<PropertySource<?>> propertySources = loader.load("domain", resource);
            MutablePropertySources envSources = environment.getPropertySources();

            for (PropertySource<?> ps : propertySources) {
                // 중복된 PropertySource 이름을 피합니다.
                if (envSources.contains(ps.getName())) {
                    if (log.isDebugEnabled()) {
                        log.debug("PropertySource '{}'가 이미 존재합니다. 건너뜁니다.", ps.getName());
                    }
                    continue;
                }
                // 기존 애플리케이션 속성 값을 망가뜨리지 않도록 마지막에 추가합니다.
                envSources.addLast(ps);
                if (log.isInfoEnabled()) {
                    log.info("도메인 속성을 {}에서 PropertySource '{}'로 로드했습니다.", resource, ps.getName());
                }
            }
        } catch (IOException e) {
            log.warn("{}", resource, e);
            log.warn("domain.yml를 {}에서 로드하는 데 실패했습니다.", resource);
        }
    }

    private Resource resolveResource(String path) {
        try {
            // 먼저 파일 시스템 경로로 시도
            File file = new File(path);
            if (file.exists()) {
                return new FileSystemResource(file);
            }
        } catch (Exception ignored) {
            // eat
        }

        // ResourceLoader를 통해 시도(예: classpath: 등 지원될 수 있음)
        try {
            Resource res = resourceLoader.getResource(path);
            if (res != null && res.exists()) {
                return res;
            }
        } catch (Exception ignored) {
            // eat
        }

        // 기본: 클래스패스 상대 경로
        return new ClassPathResource(path);
    }
}