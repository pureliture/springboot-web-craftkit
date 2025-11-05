package com.teststrategy.multimodule.maven.sf.framework.exception;

/**
 * 비즈니스 규칙 위반을 표현하는 예외입니다.
 * 상태 코드는 주어진 {@link ErrorCode} 구현체에 의해 결정됩니다.
 */
public class BusinessLogicException extends CraftkitBusinessException {

    public BusinessLogicException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessLogicException(ErrorCode errorCode, Object args) {
        super(errorCode, args);
    }

    public BusinessLogicException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public BusinessLogicException(ErrorCode errorCode, Object args, Throwable cause) {
        super(errorCode, args, cause);
    }
}
