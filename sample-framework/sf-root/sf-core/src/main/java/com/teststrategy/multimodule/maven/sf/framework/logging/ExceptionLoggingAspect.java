package com.teststrategy.multimodule.maven.sf.framework.logging;

import com.teststrategy.multimodule.maven.sf.framework.application.annotation.AutoLogging;
import com.teststrategy.multimodule.maven.sf.framework.application.setting.LoggingProperties;
import com.teststrategy.multimodule.maven.sf.framework.exception.model.CraftkitBaseException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;

/**
 * 예외 및 진입/반환 로깅 AOP Aspect (SRP 분리)
 * - Controller/Service/Repository 경계와 @AutoLogging 대상으로 메서드 호출을 디버그 레벨로 로깅합니다.
 * - 인자/반환값 로깅은 프로퍼티로 제어합니다. 기본은 비활성화입니다.
 * - @Service / @Repository 경계에서 래핑되지 않은 예외 누수를 경고합니다.
 * - CraftkitBaseException 으로 이미 변환(wrap)된 예외는 별도로 로깅하지 않습니다.
 */
@Aspect
public class ExceptionLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ExceptionLoggingAspect.class);

    private final LoggingProperties properties;
    private final ThreadLocal<Integer> callDepth = new ThreadLocal<>();

    public ExceptionLoggingAspect(LoggingProperties properties) {
        this.properties = properties;
    }

    // Pointcuts
    @Pointcut("within(@org.springframework.stereotype.Controller *) || within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerLayer() {}

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void serviceLayer() {}

    @Pointcut("within(@org.springframework.stereotype.Repository *)")
    public void repositoryLayer() {}

    @Pointcut("controllerLayer() || serviceLayer() || repositoryLayer()")
    public void appLayers() {}

    @Pointcut("within(@" +
            "com.teststrategy.multimodule.maven.sf.framework.application.annotation.AutoLogging" +
            " *)")
    public void autoLoggingOnType() {}

    @Pointcut("@annotation(com.teststrategy.multimodule.maven.sf.framework.application.annotation.AutoLogging)")
    public void autoLoggingOnMethod() {}

    @Pointcut("appLayers() || autoLoggingOnType() || autoLoggingOnMethod()")
    public void loggingTargets() {}

    // Around advice for normalized method logging
    @Around("loggingTargets()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        if (!log.isDebugEnabled()) {
            return pjp.proceed();
        }

        stepIn();
        long start = System.currentTimeMillis();

        String signature = pjp.getSignature().toLongString();

        if (properties.isIncludeArguments()) {
            tryLogArguments(pjp);
        }

        log.debug("{}-> {} {}     @{} {{", footprint(), LocalTime.now(), "      ", signature);
        Object result;
        try {
            result = pjp.proceed();
        } finally {
            long took = System.currentTimeMillis() - start;
            log.debug("{}<- {} {}ms }} @{}", footprint(), LocalTime.now(), String.format("%6d", took), signature);
            stepOut();
        }

        if (properties.isIncludeReturns()) {
            tryLogReturn(pjp, result);
        }

        return result;
    }

    /**
     * @Service 또는 @Repository에서 발생한 예외를 감지
     */
    @AfterThrowing(
            pointcut = "serviceLayer() || repositoryLayer()",
            throwing = "ex"
    )
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        // 이미 Craftkit 예외로 변환된 경우: 중앙 핸들러에서 처리/로깅 예정이므로 여기서는 중복 로깅하지 않음
        if (ex instanceof CraftkitBaseException) {
            return;
        }

        // 래핑되지 않은 예외 누수 경고
        log.warn("[Architecture Violation] Potential unwrapped exception: {} at {}. This should be wrapped into CraftkitBaseException.",
                ex.getClass().getName(), joinPoint.getSignature().toShortString());
    }

    // ===== helpers =====
    private void stepIn() {
        Integer depth = callDepth.get();
        callDepth.set(depth == null ? 1 : depth + 1);
    }

    private void stepOut() {
        Integer depth = callDepth.get();
        callDepth.set(depth == null ? 0 : Math.max(0, depth - 1));
    }

    private String footprint() {
        Integer depth = callDepth.get();
        if (depth == null || depth <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < 11; i++) {
            if (depth == 1) sb.append('-');
            else if (i < depth) sb.append('.');
            else sb.append(' ');
        }
        return sb.toString();
    }

    private void tryLogArguments(ProceedingJoinPoint joinPoint) {
        try {
            String className = joinPoint.getSignature().getDeclaringTypeName();
            String methodName = joinPoint.getSignature().getName();
            CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
            Object[] args = joinPoint.getArgs();
            String[] argNames = codeSignature.getParameterNames();
            Class<?>[] argTypes = codeSignature.getParameterTypes();

            for (int i = 0; i < args.length; i++) {
                log.debug("{}.{} argument[{}] name: {}, type: {}, value: {}",
                        className, methodName, i,
                        (argNames == null ? "null" : argNames[i]),
                        argTypes[i].getTypeName(),
                        args[i] == null ? "null" : args[i]);
            }
        } catch (Exception e) {
            log.debug("argument logging error");
        }
    }

    private void tryLogReturn(ProceedingJoinPoint joinPoint, Object result) {
        try {
            String className = joinPoint.getSignature().getDeclaringTypeName();
            String methodName = joinPoint.getSignature().getName();
            MethodSignature ms = (MethodSignature) joinPoint.getSignature();
            Class<?> returnType = ms.getReturnType();
            log.debug("{}.{} return - type: {}, value: {}", className, methodName, returnType.getTypeName(),
                    result == null ? "null" : result);
        } catch (Exception e) {
            log.debug("return logging error");
        }
    }
}
