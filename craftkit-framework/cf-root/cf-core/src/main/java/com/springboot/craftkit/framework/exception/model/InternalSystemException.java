package com.springboot.craftkit.framework.exception.model;

/**
 * 500 Internal Server Error 수준의 내부 프로그래밍 오류/예상치 못한 실패를 표현하는 예외입니다.
 */
public class InternalSystemException extends CraftkitSystemException {

    public InternalSystemException() {
        super(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    public InternalSystemException(Object args) {
        super(CommonErrorCode.INTERNAL_SERVER_ERROR, args);
    }

    public InternalSystemException(Throwable cause) {
        super(CommonErrorCode.INTERNAL_SERVER_ERROR, cause);
    }

    public InternalSystemException(Object args, Throwable cause) {
        super(CommonErrorCode.INTERNAL_SERVER_ERROR, args, cause);
    }

    // 필요 시 더 구체적인 ErrorCode 허용
    public InternalSystemException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InternalSystemException(ErrorCode errorCode, Object args) {
        super(errorCode, args);
    }

    public InternalSystemException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public InternalSystemException(ErrorCode errorCode, Object args, Throwable cause) {
        super(errorCode, args, cause);
    }
}
