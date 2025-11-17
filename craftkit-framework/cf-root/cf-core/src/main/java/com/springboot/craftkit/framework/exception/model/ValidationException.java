package com.springboot.craftkit.framework.exception.model;

/**
 * 비즈니스 관점에서의 "검증" 실패(도메인 규칙 유효성 점검 포함)를 표현하는 예외입니다.
 *
 * API 설계에 따라 비즈니스 오류를 HTTP 2xx(주로 200 OK) 응답으로 인코딩하는 경우가 있으며,
 * 이때 기본 ErrorCode는 {@link CommonErrorCode#BUSINESS_ERROR_OK} 를 사용합니다.
 * 필요 시 도메인 전용 {@link ErrorCode}를 전달해 다른 상태/메시지를 사용할 수 있습니다.
 */
public class ValidationException extends CraftkitBusinessException {

    // 기본: 200 OK로 인코딩되는 비즈니스 검증 오류
    public ValidationException() {
        super(CommonErrorCode.BUSINESS_ERROR_OK);
    }

    public ValidationException(Object args) {
        super(CommonErrorCode.BUSINESS_ERROR_OK, args);
    }

    public ValidationException(Throwable cause) {
        super(CommonErrorCode.BUSINESS_ERROR_OK, cause);
    }

    public ValidationException(Object args, Throwable cause) {
        super(CommonErrorCode.BUSINESS_ERROR_OK, args, cause);
    }

    // 필요 시 커스텀 비즈니스 ErrorCode 사용
    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ValidationException(ErrorCode errorCode, Object args) {
        super(errorCode, args);
    }

    public ValidationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public ValidationException(ErrorCode errorCode, Object args, Throwable cause) {
        super(errorCode, args, cause);
    }
}
