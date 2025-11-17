package com.teststrategy.multimodule.maven.sf.framework.exception.model;

/**
 * 비즈니스 규칙 위반을 표현하는 예외의 상위 추상 클래스입니다.
 * 비즈니스 도메인에서 파생되는 구체 예외들은 이 클래스를 상속합니다.
 *
 * 설계 배경: Business vs System의 최상위 축 중 Business 측에 해당합니다.
 */
public abstract class CraftkitBusinessException extends CraftkitBaseException {

    protected CraftkitBusinessException(ErrorCode errorCode, Object args, Throwable cause) {
        super(errorCode, args, cause);
    }

    protected CraftkitBusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    protected CraftkitBusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected CraftkitBusinessException(ErrorCode errorCode, Object args) {
        super(errorCode, args);
    }
}
