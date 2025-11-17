package com.springboot.craftkit.config;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.springboot.craftkit.framework.application.shutdown.ShutdownSupport;
import com.springboot.craftkit.framework.util.MessageUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.netflix.hystrix.HystrixCircuitBreakerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.springboot.craftkit.framework.hystrix.RequestScopeHystrixConcurrencyStrategy;
import com.springboot.craftkit.framework.hystrix.shutdown.HystrixDownHelper;

import java.util.function.UnaryOperator;


@Slf4j
@Configuration
@Import(HystrixCircuitBreakerConfiguration.class)
public class HystrixConfig {

    private final ObjectProvider<ShutdownSupport> shutdownSupportProvider;

    public HystrixConfig(ObjectProvider<ShutdownSupport> shutdownSupportProvider) {
        this.shutdownSupportProvider = shutdownSupportProvider;

        customizeStackTraceMessageOfHystrixRuntimeException();
    }

    private void customizeStackTraceMessageOfHystrixRuntimeException() {

        UnaryOperator<Throwable> unary = throwable -> {
            try {
                if (throwable instanceof HystrixRuntimeException exception) {
                    Throwable child = exception.getFallbackException();
                    if (exception.getFallbackException() != null && child != exception) {
                        return exception.getFallbackException();
                    }
                }
            } catch (Exception e) {
                return null;
            }
            return null;
        };

        MessageUtil.registerOtherCausedExtractor("fallback by", unary);
    }

    @Bean("requestScopeHystrixConcurrencyStrategy")
    public RequestScopeHystrixConcurrencyStrategy requestScopeHystrixConcurrencyStrategy() {
        return new RequestScopeHystrixConcurrencyStrategy();
    }

    @PostConstruct
    public void initialize() {
        shutdownSupportProvider.ifAvailable(shutdownSupport -> shutdownSupport.addShutdownHelper(new HystrixDownHelper()));
    }
}
