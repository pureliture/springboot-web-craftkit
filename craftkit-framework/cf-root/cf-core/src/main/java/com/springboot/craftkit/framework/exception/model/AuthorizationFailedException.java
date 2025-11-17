package com.springboot.craftkit.framework.exception.model;

/**
 * 403 Forbidden 수준의 인가(Authorization) 실패를 표현하는 예외입니다.
 */
public class AuthorizationFailedException extends CraftkitSystemException {

    public AuthorizationFailedException() {
        super(AuthErrorCode.ACCESS_DENIED);
    }

    public AuthorizationFailedException(Object args) {
        super(AuthErrorCode.ACCESS_DENIED, args);
    }

    public AuthorizationFailedException(Throwable cause) {
        super(AuthErrorCode.ACCESS_DENIED, cause);
    }

    public AuthorizationFailedException(Object args, Throwable cause) {
        super(AuthErrorCode.ACCESS_DENIED, args, cause);
    }
}
