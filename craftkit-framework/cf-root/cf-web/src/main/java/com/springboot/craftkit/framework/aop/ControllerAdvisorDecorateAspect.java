package com.springboot.craftkit.framework.aop;

import com.springboot.craftkit.framework.util.MessageUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.Order;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import java.util.*;

/**
 * Aspect class for injecting ControllerAdvisor at runtime.
 * <p>
 * Collects created ControllerAdvisor beans and executes assist() sequentially 
 * according to org.springframework.core.Ordered.getOrder() criteria.
 * <p>
 * Pointcuts are defined for RequestMapping, GetMapping, PostMapping, PutMapping, DeleteMapping
 * annotations on Controller classes.
 */
@SuppressWarnings("EmptyMethod")
@Aspect
@Order(0)
public class ControllerAdvisorDecorateAspect implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(ControllerAdvisorDecorateAspect.class);

    final ObjectProvider<ControllerAdvisor> controllerAdvisorProvider;

    public ControllerAdvisorDecorateAspect(ObjectProvider<ControllerAdvisor> httpBizInterceptorProvider) {
        this.controllerAdvisorProvider = httpBizInterceptorProvider;
    }

    @Override
    public void afterPropertiesSet() {
        controllerAdvisorProvider.orderedStream().forEach(bean -> {
            Class<?> clazz = ClassUtils.getUserClass(bean);
            log.info("ControllerAdvisor bean {} detected, ControllerAdvisorDecorateAspect is activated.", clazz.getName());
        });
    }

    @Pointcut("execution(* com.springboot.craftkit..*.*(..))")
    public void frameworkPackage() {
        // ignore
    }

    @Pointcut("execution(* *..*Controller.*(..))")
    public void controllerClass() {
        // ignore
    }

    @Pointcut("!frameworkPackage() && controllerClass() && @annotation(org.springframework.web.bind.annotation.GetMapping)")
    public void getMapping() {
        // ignore
    }

    @Pointcut("!frameworkPackage() && controllerClass() && @annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void postMapping() {
        // ignore
    }

    @Pointcut("!frameworkPackage() && controllerClass() && @annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void putMapping() {
        // ignore
    }

    @Pointcut("!frameworkPackage() && controllerClass() && @annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void deleteMapping() {
        // ignore
    }

    @Pointcut("!frameworkPackage() && controllerClass() && @annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void requestMapping() {
        // ignore
    }

    @Around(value = "getMapping() || postMapping() || putMapping() || deleteMapping() || requestMapping()")
    public Object around(ProceedingJoinPoint jp) throws Throwable {

        if (controllerAdvisorProvider != null) {
            final HttpServletRequest request = getHttpServletRequest();

            // headers 
            Map<String, List<String>> headerMap = extractHeaders(request);

            // path variable 
            String mappingUrl = (String) Objects.requireNonNull(request).getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            Map<String, String> pathVariableMap = extractPathVariables(request, mappingUrl);

            // query or form parameters 
            Map<String, List<String>> queryParameterMap = extractParameters(request);

            controllerAdvisorProvider.orderedStream()
                    .forEach(action -> action.preController(request, jp, mappingUrl, headerMap, pathVariableMap, queryParameterMap));

            try {
                Object returnObject = jp.proceed();
                controllerAdvisorProvider.orderedStream()
                        .forEach(action -> action.postReturn(request, jp, mappingUrl, headerMap, pathVariableMap, queryParameterMap, returnObject));
                return returnObject;
            } catch (Exception exception) {
                controllerAdvisorProvider.orderedStream()
                        .forEach(action -> action.postThrowing(request, jp, mappingUrl, headerMap, pathVariableMap, queryParameterMap, exception));
                throw exception;
            }
        }

        return jp.proceed();

    }

    private Map<String, List<String>> extractHeaders(final HttpServletRequest request) {

        try {
            Map<String, List<String>> headerMap = new LinkedCaseInsensitiveMap<>();
            Enumeration<String> keyEnumeration = request.getHeaderNames();

            while (keyEnumeration.hasMoreElements()) {
                String header = keyEnumeration.nextElement();
                Enumeration<String> valueEnumeration = request.getHeaders(header);

                if (valueEnumeration != null) {
                    headerMap.put(header, Collections.list(valueEnumeration));
                } else {
                    headerMap.put(header, Collections.emptyList());
                }
            }
            return headerMap;
        } catch (Exception e) {
            log.warn("ControllerAdvisorDecorateAspect.extractHeaders error {}",
                    MessageUtil.getStackTraceString(e));
            return Collections.emptyMap();
        }

    }

    private Map<String, List<String>> extractParameters(HttpServletRequest request) {

        try {
            Map<String, String[]> parametersArrayValue = request.getParameterMap();
            Map<String, List<String>> parametersListValue = new HashMap<>();
            parametersArrayValue.keySet().forEach(key -> {
                String[] values = parametersArrayValue.get(key);

                if (values == null) {
                    parametersListValue.put(key, Collections.emptyList());
                } else {
                    parametersListValue.put(key, Arrays.asList(values));
                }
            });

            return parametersListValue;
        } catch (Exception e) {
            log.warn("ControllerAdvisorDecorateAspect.extractParameters error {}",
                    MessageUtil.getStackTraceString(e));
            return Collections.emptyMap();
        }
    }

    private Map<String, String> extractPathVariables(final HttpServletRequest request, String mappingUrl) {

        try {
            if (request == null)
                return Collections.emptyMap();

            String contextPath = request.getContextPath();
            String mappingUrlWithContextPath = contextPath + mappingUrl;
            String requestUrl = request.getRequestURI();

            AntPathMatcher pathMatcher = new AntPathMatcher();
            return pathMatcher.extractUriTemplateVariables(mappingUrlWithContextPath, requestUrl);
        } catch (Exception e) {
            log.warn("ControllerAdvisorDecorateAspect.extractPathVariables error {}",
                    MessageUtil.getStackTraceString(e));
            return Collections.emptyMap();
        }
    }

    private HttpServletRequest getHttpServletRequest() {

        try {
            return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        } catch (Exception e) {
            return null;
        }
    }

}
