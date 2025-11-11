package com.teststrategy.multimodule.maven.sf.framework.logging;

import com.teststrategy.multimodule.maven.sf.framework.exception.model.CraftkitBaseException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 예외 로깅 AOP Aspect
 * - @Service / @Repository 경계에서 래핑되지 않은 예외 누수를 경고합니다.
 * - CraftkitBaseException 으로 이미 변환(wrap)된 예외는 별도로 로깅하지 않습니다.
 */
@Aspect
@Component
public class ExceptionLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ExceptionLoggingAspect.class);

    /**
     * @Service 또는 @Repository에서 발생한 예외를 감지
     */
    @AfterThrowing(
            pointcut = "within(@org.springframework.stereotype.Service *) || within(@org.springframework.stereotype.Repository *)",
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
}
