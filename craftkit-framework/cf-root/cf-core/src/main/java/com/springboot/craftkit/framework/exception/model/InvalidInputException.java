package com.springboot.craftkit.framework.exception.model;

/**
 * 400 Bad Request 수준의 클라이언트 입력 오류(유효성 검증 실패, 타입 불일치, JSON 파싱 실패 등)에 대한
 * 카테고리 예외입니다.
 */
public class InvalidInputException extends CraftkitSystemException {

    public InvalidInputException() {
        super(CommonErrorCode.INVALID_INPUT_VALUE);
    }

    public InvalidInputException(Object args) {
        super(CommonErrorCode.INVALID_INPUT_VALUE, args);
    }

    public InvalidInputException(Throwable cause) {
        super(CommonErrorCode.INVALID_INPUT_VALUE, cause);
    }

    public InvalidInputException(Object args, Throwable cause) {
        super(CommonErrorCode.INVALID_INPUT_VALUE, args, cause);
    }
}
