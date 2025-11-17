package com.springboot.craftkit.framework.exception.model;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Craftkit 프레임워크 공통 오류 코드 (도메인 비의존적)
 */
@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    // 200 OK (Business-level error encoded in 2xx responses)
    BUSINESS_ERROR_OK(HttpStatus.OK, "C200", "exception.business.error.ok"),

    // 400 Bad Request
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "exception.common.invalid.input"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C002", "exception.common.invalid.type"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C003", "exception.common.method.not.allowed"),
    INVALID_JSON_FORMAT(HttpStatus.BAD_REQUEST, "C004", "exception.common.invalid.json"),

    // 404 Not Found
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C005", "exception.common.resource.not.found"), // 도메인에서 사용될 수 있음
    NO_HANDLER_FOUND(HttpStatus.NOT_FOUND, "C006", "exception.common.no.handler.found"),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C999", "exception.common.internal.server.error"),

    // 503 Service Unavailable
    INFRASTRUCTURE_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "C503", "exception.common.infrastructure.error"),
    CIRCUIT_BREAKER_OPEN(HttpStatus.SERVICE_UNAVAILABLE, "C504", "exception.cloud.circuit.breaker.open");

    private final HttpStatus httpStatus;
    private final String code;
    private final String messageKey;
}
