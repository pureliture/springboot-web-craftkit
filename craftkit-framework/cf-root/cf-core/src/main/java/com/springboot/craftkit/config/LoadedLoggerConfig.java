package com.springboot.craftkit.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadedLoggerConfig {

    private static final Logger log = LoggerFactory.getLogger(LoadedLoggerConfig.class);

    @Bean
    public CommandLineRunner loadedLoggerRunner(ApplicationContext ctx) {

        return args -> {
            // 원하는 스코프로 제한하려면 필터링 조건 추가 가능 (@Configuration만 등)
            String[] beanNames = ctx.getBeanDefinitionNames();
            for (String name : beanNames) {
                Class<?> type = ctx.getType(name);
                if (type != null && type.isAnnotationPresent(Configuration.class)) {
                    log.info("{} loaded", type.getName());
                }
            }
        };
    }
}