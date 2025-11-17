package com.springboot.craftkit.framework.response;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * 표준 API 오류 응답 DTO
 * - 일관된 오류 응답 포맷 제공
 * - i18n 메시지, 에러 코드, 필드 검증 오류 지원
 */
public class ApiErrorResponse {

    private final ZonedDateTime timestamp;
    private final int status;         // HTTP 상태 코드 (e.g., 400)
    private final String errorCode;   // 고유 식별 코드 (e.g., "C001")
    private final String message;     // i18n 기반 사용자 메시지
    private final List<FieldErrorDetail> fieldErrors; // Validation 오류 상세

    // 일반 오류용 생성자
    public ApiErrorResponse(int status, String errorCode, String message) {
        this.timestamp = ZonedDateTime.now();
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.fieldErrors = null;
    }

    // Validation 오류용 생성자
    public ApiErrorResponse(int status, String errorCode, String message, List<FieldErrorDetail> fieldErrors) {
        this.timestamp = ZonedDateTime.now();
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public List<FieldErrorDetail> getFieldErrors() {
        return fieldErrors;
    }

    /**
     * 필드 유효성 검사 오류 상세
     */
    public static class FieldErrorDetail {
        private final String field;         // 오류 필드명 (e.g., "username")
        private final String rejectedValue; // 거부된 값 (e.g., "u")
        private final String reason;        // i18n 오류 메시지

        public FieldErrorDetail(String field, String rejectedValue, String reason) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.reason = reason;
        }

        public String getField() {
            return field;
        }

        public String getRejectedValue() {
            return rejectedValue;
        }

        public String getReason() {
            return reason;
        }
    }
}
