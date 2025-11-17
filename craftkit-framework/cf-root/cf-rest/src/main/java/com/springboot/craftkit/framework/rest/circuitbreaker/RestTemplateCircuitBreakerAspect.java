package com.springboot.craftkit.framework.rest.circuitbreaker;

import com.springboot.craftkit.framework.rest.client.CircuitBreakerInstanceNamer;
import com.springboot.craftkit.framework.rest.client.DomainApiContext;
import com.springboot.craftkit.framework.rest.setting.SfRestCircuitBreakerProperties;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Optional;
import java.util.function.Function;


@Aspect
@Order(Integer.MIN_VALUE)
public class RestTemplateCircuitBreakerAspect {

    private final CircuitBreakerFactory<?, ?> factory;
    private final SfRestCircuitBreakerProperties properties;
    private final CircuitBreakerInstanceNamer namer;

    public RestTemplateCircuitBreakerAspect(CircuitBreakerFactory<?, ?> factory,
                                            SfRestCircuitBreakerProperties properties,
                                            CircuitBreakerInstanceNamer namer) {
        this.factory = factory;
        this.properties = properties;
        this.namer = namer;
    }

    @Pointcut("execution(* org.springframework.web.client.RestTemplate.set*(..)) || " +
            "execution(* org.springframework.web.client.RestTemplate.get*(..)) && args() && !execution(* org.springframework.web.client.RestTemplate.getForEntity(..)) || " +
            "execution(* org.springframework.web.client.RestTemplate.acceptHeaderRequestCallback(..)) || " +
            "execution(* org.springframework.web.client.RestTemplate.httpEntityCallback(..)) || " +
            "execution(* org.springframework.web.client.RestTemplate.responseEntityExtractor(..))")
    public void exceptMethod() {}

    @Pointcut("within(org.springframework.web.client.RestTemplate+) && !exceptMethod()")
    public void anyRestTemplateMethod() {}

    @Around("anyRestTemplateMethod()")
    public Object aroundRestOperations(ProceedingJoinPoint pjp) throws Throwable {
        if (!properties.isEnabled() || properties.getMode() != SfRestCircuitBreakerProperties.Mode.AOP) {
            return pjp.proceed();
        }
        Object[] args = pjp.getArgs();
        if (ArrayUtils.isEmpty(args)) {
            return pjp.proceed();
        }
        Optional<Invocation> invocation = resolveInvocation(pjp);
        if (!invocation.isPresent()) {
            return pjp.proceed();
        }
        final Invocation inv = invocation.get();
        final String instanceName = namer.name(new SimpleHttpRequest(inv.method, inv.uri));
        CircuitBreaker cb = factory.create(instanceName);
        try {
            Function<Throwable, Object> fallback = extractFallback(args);
            if (fallback != null) {
                return cb.run(() -> {
                    try {
                        return pjp.proceed();
                    } catch (Throwable t) {
                        throw new RuntimeException(t);
                    }
                }, t -> fallback.apply(unwrap(t)));
            }
            return cb.run(() -> {
                try {
                    return pjp.proceed();
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            });
        } finally {
            // Clear DomainApiContext to avoid leaking across calls
            DomainApiContext.clear();
        }
    }

    private static Throwable unwrap(Throwable t) {
        if (t instanceof RuntimeException && t.getCause() != null) return t.getCause();
        return t;
    }

    private Optional<Invocation> resolveInvocation(ProceedingJoinPoint pjp) {
        Object[] args = pjp.getArgs();
        // Common RestTemplate signatures: (String url, ...), (URI uri, ...), (RequestEntity req, ...)
        // Try to find URI and optionally HttpMethod
        URI uri = null;
        HttpMethod method = null;
        Object first = args[0];
        if (first instanceof String) {
            try { uri = URI.create((String) first); } catch (Exception ignore) {}
        } else if (first instanceof URI) {
            uri = (URI) first;
        } else if (isRequestEntity(first)) {
            try {
                Method getUrl = first.getClass().getMethod("getUrl");
                Method getMethod = first.getClass().getMethod("getMethod");
                Object u = getUrl.invoke(first);
                Object m = getMethod.invoke(first);
                if (u instanceof URI) uri = (URI) u;
                if (m instanceof HttpMethod) method = (HttpMethod) m;
            } catch (Exception ignore) {}
        }
        // Some methods pass HttpMethod as 2nd arg (e.g., exchange)
        if (method == null) {
            for (Object a : args) {
                if (a instanceof HttpMethod) { method = (HttpMethod) a; break; }
            }
        }
        if (uri == null) return Optional.empty();
        if (method == null) method = HttpMethod.GET;
        return Optional.of(new Invocation(uri, method));
    }

    private boolean isRequestEntity(Object o) {
        if (o == null) return false;
        return o.getClass().getName().equals("org.springframework.http.RequestEntity");
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private Function<Throwable, Object> extractFallback(Object[] args) {
        for (Object a : args) {
            if (a instanceof Function) {
                return (Function<Throwable, Object>) a;
            }
        }
        return null;
    }

    private static class Invocation {
        final URI uri; final HttpMethod method;
        Invocation(URI uri, HttpMethod method) { this.uri = uri; this.method = method; }
    }

    private static class SimpleHttpRequest extends HttpRequestWrapper implements HttpRequest {
        private final HttpMethod method;
        SimpleHttpRequest(HttpMethod method, URI uri) {
            super(new DummyRequest(method, uri));
            Assert.notNull(method, "method");
            this.method = method;
        }
        @Override public HttpMethod getMethod() { return method; }
    }

    private static class DummyRequest implements HttpRequest {
        private final URI uri;
        private final HttpMethod method;
        DummyRequest(HttpMethod method, URI uri) {
            this.method = (method != null ? method : HttpMethod.GET);
            this.uri = uri;
        }
        @Override public HttpMethod getMethod() { return method; }
        public String getMethodValue() { return method.name(); }
        @Override public URI getURI() { return uri; }
        @Override public HttpHeaders getHeaders() { return new HttpHeaders(); }
    }
}
