package com.springboot.craftkit.framework.exception.model;

import org.springframework.core.NestedRuntimeException;

import lombok.Getter;

/**
 * Craftkit 프레임워크의 모든 커스텀 Runtime 예외의 최상위 부모 클래스입니다.
 * 모든 예외는 ErrorCode를 포함해야 합니다.
 */
@Getter
public abstract class CraftkitBaseException extends NestedRuntimeException {

    private final ErrorCode errorCode;
    private final Object args; // i18n 메시지 파라미터 (S16)

    /**
     * @param errorCode 이 예외의 고유한 오류 코드 및 메타데이터
     * @param args i18n 메시지 포맷팅을 위한 동적 인자 (S16)
     * @param cause 이 예외를 유발한 근본 원인 (S8, S82)
     */
    protected CraftkitBaseException(ErrorCode errorCode, Object args, Throwable cause) {
        // 메시지 키를 개발자용 메시지로 사용 (S5)
        super(errorCode.getMessageKey(), cause);
        this.errorCode = errorCode;
        this.args = args;
    }

    // 다양한 생성자 오버로딩 (cause가 없는 경우, args가 없는 경우 등)
    protected CraftkitBaseException(ErrorCode errorCode, Throwable cause) {
        this(errorCode, null, cause);
    }

    protected CraftkitBaseException(ErrorCode errorCode) {
        this(errorCode, null, null);
    }

    protected CraftkitBaseException(ErrorCode errorCode, Object args) {
        this(errorCode, args, null);
    }
}
