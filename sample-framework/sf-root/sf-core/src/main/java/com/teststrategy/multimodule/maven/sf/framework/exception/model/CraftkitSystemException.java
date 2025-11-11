package com.teststrategy.multimodule.maven.sf.framework.exception.model;

/**
 * 시스템(기술) 측면의 실패를 표현하는 예외의 상위 추상 클래스입니다.
 * 외부 시스템 장애, 내부 프로그래밍 오류, 클라이언트 요청 오류(4xx) 등
 * 비즈니스 규칙 위반이 아닌 모든 기술적 실패 계층의 루트입니다.
 *
 * 설계 배경: Business vs System의 최상위 축 중 System 측에 해당합니다.
 */
public abstract class CraftkitSystemException extends CraftkitBaseException {

    protected CraftkitSystemException(ErrorCode errorCode, Object args, Throwable cause) {
        super(errorCode, args, cause);
    }

    protected CraftkitSystemException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    protected CraftkitSystemException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected CraftkitSystemException(ErrorCode errorCode, Object args) {
        super(errorCode, args);
    }
}
