package com.springboot.craftkit.framework.exception.model;

/**
 * 401 Unauthorized 수준의 인증 실패를 표현하는 예외입니다.
 */
public class AuthenticationFailedException extends CraftkitSystemException {

    public AuthenticationFailedException() {
        super(AuthErrorCode.AUTHENTICATION_FAILED);
    }

    public AuthenticationFailedException(Object args) {
        super(AuthErrorCode.AUTHENTICATION_FAILED, args);
    }

    public AuthenticationFailedException(Throwable cause) {
        super(AuthErrorCode.AUTHENTICATION_FAILED, cause);
    }

    public AuthenticationFailedException(Object args, Throwable cause) {
        super(AuthErrorCode.AUTHENTICATION_FAILED, args, cause);
    }
}
