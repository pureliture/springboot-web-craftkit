package com.teststrategy.multimodule.maven.config.context;

import com.teststrategy.multimodule.maven.sf.framework.application.SampleFrameworkAnnotationBeanNameGenerator;
import com.teststrategy.multimodule.maven.sf.framework.resource.RetryableUrlResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

import java.util.Optional;


/**
 * Application 기동시 ApplicationContextInitializer 를 사용하여, application context 초기화를 수행한다.
 * SpringApplication 의 초기화를 수행해야하므로 spring.factories 에서 org.springframework.context.ApplicationContextInitializer 으로
 * Configuration 을 수행해야한다.
 */
@Configuration
public class SampleFrameworkCoreApplicationContextInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered  {

    static Logger log = LoggerFactory.getLogger(SampleFrameworkCoreApplicationContextInitializer.class);

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * Spring 에서는 기본으로 AnnotationBeanNameGenerator 를 사용하는데, BeanName 이 중복될 경우 기동오류가 발생하므로,
     *  BeanName 이 중복될 경우 FullyQualifiedName(package+class)로 BeanName 을 생성한다.
     *
     * 단, @Component, @Service, @Repository, ... 등의 Class 기반 BeanName 에 적용되며 @Bean 을 사용하는 MethodBean에는
     * 적용되지 않는다.
     */
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {

        registerSingletonBeanNameGenerator(applicationContext);

        setRetryableUrlResourcePolicy(applicationContext);
    }


    // URL 리소스로 컨피그서버의 리소스를 가져올때 일시적 장애가 발생하면 Retry 정책을 동일하게 적용한다.
    private void setRetryableUrlResourcePolicy(ConfigurableApplicationContext applicationContext) {
        Environment environment = applicationContext.getEnvironment();

        try {
            Optional.ofNullable(environment.getProperty("spring.cloud.config.retry.initial-interval", long.class))
                    .ifPresent(RetryableUrlResource::setInitialInterval);
        } catch(Exception e) {
            // eat
        }

        try {
            Optional.ofNullable(environment.getProperty("spring.cloud.config.retry.multiplier", double.class))
                    .ifPresent(RetryableUrlResource::setMultiplier);
        } catch (Exception e) {
            // eat
        }

        try {
            Optional.ofNullable(environment.getProperty("spring.cloud.config.retry.max-interval", long.class))
                    .ifPresent(RetryableUrlResource::setMaxInterval);
        } catch (Exception e) {
            // eat
        }

        try {
            Optional.ofNullable(environment.getProperty("spring.cloud.config.retry.max-attempts", int.class))
                    .ifPresent(RetryableUrlResource::setMaxAttempts);
        } catch (Exception e) {
            // eat
        }
    }

    private void registerSingletonBeanNameGenerator(ConfigurableApplicationContext applicationContext) {
        log.info("ApplicationContextInitializer: BeanNameGenerator SampleFrameworkAnnotationBeanNameGenerator is registered!");
        applicationContext.getBeanFactory()
                .registerSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, new SampleFrameworkAnnotationBeanNameGenerator());
    }
}
