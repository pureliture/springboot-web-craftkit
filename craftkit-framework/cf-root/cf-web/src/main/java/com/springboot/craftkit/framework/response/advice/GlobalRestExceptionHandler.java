package com.springboot.craftkit.framework.response.advice;

import com.springboot.craftkit.framework.exception.model.CommonErrorCode;
import com.springboot.craftkit.framework.exception.model.CraftkitBaseException;
import com.springboot.craftkit.framework.exception.model.ErrorCode;
import com.springboot.craftkit.framework.response.ApiErrorResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalRestExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    public GlobalRestExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    // 1) 모든 CraftkitBaseException 처리 (확장 모듈의 예외 포함)
    @ExceptionHandler(CraftkitBaseException.class)
    public ResponseEntity<Object> handleCraftkitBaseException(CraftkitBaseException ex, Locale locale) {
        ErrorCode code = ex.getErrorCode();
        Object args = ex.getArgs();
        String message = resolveMessage(code.getMessageKey(), args, locale, code.getMessageKey());

        ApiErrorResponse body = new ApiErrorResponse(
                code.getHttpStatus().value(),
                code.getCode(),
                message
        );
        return new ResponseEntity<>(body, code.getHttpStatus());
    }

    // 2) 미처리 예외 폴백 핸들러 (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnknownException(Exception ex, Locale locale) {
        // 전체 스택 로그 필수
        logger.error("Unhandled exception occurred: " + ex.getMessage(), ex);

        ErrorCode code = CommonErrorCode.INTERNAL_SERVER_ERROR;
        String message = resolveMessage(code.getMessageKey(), null, locale, "Internal server error");

        ApiErrorResponse body = new ApiErrorResponse(
                code.getHttpStatus().value(),
                code.getCode(),
                message
        );
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ====== Validation (@Valid) ======
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Locale locale = request.getLocale();

        List<ApiErrorResponse.FieldErrorDetail> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> new ApiErrorResponse.FieldErrorDetail(
                        err.getField(),
                        err.getRejectedValue() != null ? err.getRejectedValue().toString() : "null",
                        messageSource.getMessage(err, locale)
                ))
                .collect(Collectors.toList());

        ErrorCode code = CommonErrorCode.INVALID_INPUT_VALUE; // VALIDATION_FAILED에 해당
        String message = resolveMessage(code.getMessageKey(), null, locale, code.getMessageKey());

        ApiErrorResponse body = new ApiErrorResponse(status.value(), code.getCode(), message, fieldErrors);
        return new ResponseEntity<>(body, headers, status);
    }

    @SuppressWarnings("removal") // keep compatibility while method is marked for removal in future
    @Override
    protected ResponseEntity<Object> handleBindException(
            BindException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        Locale locale = request.getLocale();
        ErrorCode code = CommonErrorCode.INVALID_INPUT_VALUE;
        String message = resolveMessage(code.getMessageKey(), null, locale, code.getMessageKey());
        ApiErrorResponse body = new ApiErrorResponse(status.value(), code.getCode(), message);
        return new ResponseEntity<>(body, headers, status);
    }

    // ====== Web (request/routing) ======
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        Locale locale = request.getLocale();
        ErrorCode code = CommonErrorCode.INVALID_JSON_FORMAT;
        String message = resolveMessage(code.getMessageKey(), null, locale, ex.getLocalizedMessage());
        ApiErrorResponse body = new ApiErrorResponse(status.value(), code.getCode(), message);
        return new ResponseEntity<>(body, headers, status);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            WebRequest request) {
        Locale locale = request.getLocale();
        ErrorCode code = CommonErrorCode.INVALID_TYPE_VALUE;
        Object[] args = new Object[]{ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"};
        String message = resolveMessage(code.getMessageKey(), args, locale, ex.getLocalizedMessage());
        ApiErrorResponse body = new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), code.getCode(), message);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            org.springframework.web.HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        Locale locale = request.getLocale();
        ErrorCode code = CommonErrorCode.METHOD_NOT_ALLOWED;
        String message = resolveMessage(code.getMessageKey(), null, locale, ex.getLocalizedMessage());
        ApiErrorResponse body = new ApiErrorResponse(status.value(), code.getCode(), message);
        return new ResponseEntity<>(body, headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        Locale locale = request.getLocale();
        ErrorCode code = CommonErrorCode.NO_HANDLER_FOUND;
        String message = resolveMessage(code.getMessageKey(), null, locale, ex.getLocalizedMessage());
        ApiErrorResponse body = new ApiErrorResponse(status.value(), code.getCode(), message);
        return new ResponseEntity<>(body, headers, status);
    }

    // ====== i18n 유틸 ======
    private String resolveMessage(String key, Object args, Locale locale, String defaultMessage) {
        try {
            return messageSource.getMessage(key, args instanceof Object[] ? (Object[]) args : (args == null ? null : new Object[]{args}), locale);
        } catch (NoSuchMessageException e) {
            logger.warn("i18n message key not found: " + key, e);
            return defaultMessage;
        }
    }
}
