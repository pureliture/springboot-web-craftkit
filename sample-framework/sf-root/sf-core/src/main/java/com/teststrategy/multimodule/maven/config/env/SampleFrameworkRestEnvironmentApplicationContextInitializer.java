package com.teststrategy.multimodule.maven.config.env;

import com.teststrategy.multimodule.maven.sf.framework.application.env.DomainPropertySourceLoadHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 애플리케이션 시작 시점에 조기에 Spring 환경에 domain.yml을(구성되어 있는 경우) 로드합니다.
 *
 * 속성 키: sf-rest.domain.config
 * - 값이 디렉토리인 경우, 해당 디렉토리 내에 domain.yml 파일을 시도합니다.
 * - 값이 파일 경로나 리소스 경로인 경우, 이에 따라 해석합니다.
 */
public class SampleFrameworkRestEnvironmentApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

    private static final Logger log = LoggerFactory.getLogger(SampleFrameworkRestEnvironmentApplicationContextInitializer.class);

    /**
     * 높은 우선순위로 실행되며, Spring Cloud 부트스트랩 구성이 로드된 이후이면서 대부분의 사용자 구성보다 먼저 실행됩니다.
     */
    private int order = Ordered.HIGHEST_PRECEDENCE + 11;

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        try {
            new DomainPropertySourceLoadHelper(environment, applicationContext).load();
        } catch (Exception e) {
            log.debug("Domain properties loading skipped or failed: {}", e.getMessage());
        }
    }
}
