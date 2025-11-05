package com.teststrategy.multimodule.maven.sf.framework.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 인증(Authentication) 및 인가(Authorization) 관련 오류 코드
 * TODO : 인증/인가 관련 모듈 추가 또는 분류 시 AuthErrorCode도 해당 모듈로 이동
 */
@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH001", "exception.auth.authentication.failed"),
    INVALID_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH002", "exception.auth.token.invalid"),
    EXPIRED_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH003", "exception.auth.token.expired"),
    TOKEN_SIGNATURE_FAILED(HttpStatus.UNAUTHORIZED, "AUTH004", "exception.auth.token.signature.failed"),
    TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED, "AUTH005", "exception.auth.token.malformed"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH006", "exception.auth.access.denied");

    private final HttpStatus httpStatus;
    private final String code;
    private final String messageKey;
}
