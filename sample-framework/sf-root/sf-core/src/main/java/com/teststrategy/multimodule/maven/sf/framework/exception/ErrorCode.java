package com.teststrategy.multimodule.maven.sf.framework.exception;

import org.springframework.http.HttpStatus;

/**
 * Craftkit 프레임워크의 모든 오류 코드를 위한 표준 인터페이스입니다.
 * 모든 ErrorCode 구현체(주로 Enum)는 이 인터페이스를 구현해야 합니다.
 */
public interface ErrorCode {

    /**
     * 이 오류에 해당하는 표준 HTTP 상태 코드를 반환합니다.
     */
    HttpStatus getHttpStatus();

    /**
     * 애플리케이션 전역에서 유일한 오류 식별 코드를 반환합니다.
     * (예: "C001", "AUTH002")
     */
    String getCode();

    /**
     * MessageSource (i18n)에서 오류 메시지를 조회하기 위한 메시지 키를 반환합니다.
     * (예: "exception.common.invalid.input")
     */
    String getMessageKey();
}
