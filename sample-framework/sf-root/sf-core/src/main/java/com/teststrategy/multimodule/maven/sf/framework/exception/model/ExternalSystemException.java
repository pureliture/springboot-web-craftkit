package com.teststrategy.multimodule.maven.sf.framework.exception.model;

/**
 * 503 Service Unavailable 수준의 외부 시스템(인프라) 장애를 표현하는 예외입니다.
 * DB, 외부 API, 메시지 브로커, 설정 서버 등 의존 시스템의 실패를 감쌉니다.
 */
public class ExternalSystemException extends CraftkitSystemException {

    // 기본 카테고리 코드(프레임워크 공통)
    public ExternalSystemException() {
        super(CommonErrorCode.INFRASTRUCTURE_ERROR);
    }

    public ExternalSystemException(Object args) {
        super(CommonErrorCode.INFRASTRUCTURE_ERROR, args);
    }

    public ExternalSystemException(Throwable cause) {
        super(CommonErrorCode.INFRASTRUCTURE_ERROR, cause);
    }

    public ExternalSystemException(Object args, Throwable cause) {
        super(CommonErrorCode.INFRASTRUCTURE_ERROR, args, cause);
    }

    // 보다 구체적인 ErrorCode를 사용하고 싶은 경우(모듈/도메인 정의 코드)
    public ExternalSystemException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ExternalSystemException(ErrorCode errorCode, Object args) {
        super(errorCode, args);
    }

    public ExternalSystemException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public ExternalSystemException(ErrorCode errorCode, Object args, Throwable cause) {
        super(errorCode, args, cause);
    }
}
