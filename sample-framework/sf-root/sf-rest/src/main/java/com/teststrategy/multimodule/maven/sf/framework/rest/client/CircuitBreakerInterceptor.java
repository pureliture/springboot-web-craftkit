package com.teststrategy.multimodule.maven.sf.framework.rest.client;

import com.teststrategy.multimodule.maven.sf.framework.rest.setting.CircuitBreakerProperties;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.ConfigurationNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Client interceptor which executes outbound HTTP calls within a Resilience4j CircuitBreaker.
 *
 * Ordering recommendation: place after retry and before business error handler.
 */
public class CircuitBreakerInterceptor implements ClientHttpRequestInterceptor {

    private final CircuitBreakerRegistry registry;
    private final CircuitBreakerProperties properties;
    private final CircuitBreakerInstanceNamer namer;
    private final List<Class<? extends Throwable>> ignoreExceptions;
    private final List<Class<? extends Throwable>> recordExceptions;

    public CircuitBreakerInterceptor(CircuitBreakerRegistry registry,
                                     CircuitBreakerProperties properties,
                                     CircuitBreakerInstanceNamer namer) {
        this.registry = registry;
        this.properties = properties;
        this.namer = namer;
        this.ignoreExceptions = loadClasses(properties.getIgnoreExceptions());
        this.recordExceptions = loadClasses(properties.getRecordExceptions());
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String instanceName = namer.name(request);
        CircuitBreaker cb = getOrCreate(instanceName);

        if (!cb.tryAcquirePermission()) {
            throw CallNotPermittedException.createCallNotPermittedException(cb);
        }

        long start = System.nanoTime();
        try {
            ClientHttpResponse response = execution.execute(request, body);
            cb.onSuccess(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            return response;
        } catch (IOException | RuntimeException ex) {
            Throwable effective = ex;
            if (shouldIgnore(ex)) {
                cb.onSuccess(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            } else if (shouldRecord(ex)) {
                cb.onError(System.nanoTime() - start, TimeUnit.NANOSECONDS, effective);
            } else {
                // default: record error using CB rules
                cb.onError(System.nanoTime() - start, TimeUnit.NANOSECONDS, effective);
            }
            throw ex;
        } finally {
            // Clear DomainApi context for this thread to avoid leaking to next calls
            DomainApiContext.clear();
        }
    }

    private CircuitBreaker getOrCreate(String instanceName) {
        if (StringUtils.isNotBlank(properties.getDefaultConfig())) {
            try {
                return registry.circuitBreaker(instanceName, properties.getDefaultConfig());
            } catch (ConfigurationNotFoundException ignored) {
                // fall through
            }
        }
        return registry.circuitBreaker(instanceName);
    }

    private boolean shouldIgnore(Throwable ex) {
        for (Class<? extends Throwable> c : ignoreExceptions) {
            if (c.isInstance(ex)) return true;
        }
        return false;
    }

    private boolean shouldRecord(Throwable ex) {
        if (recordExceptions == null || recordExceptions.isEmpty()) return true; // default record
        for (Class<? extends Throwable> c : recordExceptions) {
            if (c.isInstance(ex)) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static List<Class<? extends Throwable>> loadClasses(java.util.Set<String> fqns) {
        List<Class<? extends Throwable>> list = new ArrayList<>();
        if (fqns == null) return list;
        for (String fqn : fqns) {
            try {
                Class<?> clazz = Class.forName(fqn);
                if (Throwable.class.isAssignableFrom(clazz)) {
                    list.add((Class<? extends Throwable>) clazz);
                }
            } catch (ClassNotFoundException ignored) {}
        }
        return list;
    }
}
