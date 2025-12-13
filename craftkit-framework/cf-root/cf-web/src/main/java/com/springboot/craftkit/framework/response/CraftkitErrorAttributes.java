package com.springboot.craftkit.framework.response;

import com.springboot.craftkit.framework.application.constant.CommonConstants;
import com.springboot.craftkit.framework.exception.model.CraftkitBaseException;
import com.springboot.craftkit.framework.util.PropertyUtil;
import jakarta.servlet.RequestDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Custom error attributes for Craftkit framework.
 * Extends DefaultErrorAttributes to provide additional error information.
 */
@Slf4j
public class CraftkitErrorAttributes extends DefaultErrorAttributes {

    private static final String UNKNOWN = "unknown";

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {

        String errorStatus = getStatus(webRequest);
        String errorMessage = getMessage(errorStatus);
        String errorDetail = getDetail(errorMessage, webRequest);
        String errorServer = PropertyUtil.getApplicationName();

        Throwable throwable = getError(webRequest);
        String messageCode = "undefined";
        Integer httpStatus = null;

        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("errorServer", errorServer);

        if (throwable == null) {
            errorBody.put("errorCode", errorStatus);
            errorBody.put("errorMsg", errorMessage);
            errorBody.put("errorDetail", errorDetail);
        } else if (throwable instanceof CraftkitBaseException craftkitException) {
            httpStatus = craftkitException.getErrorCode().getHttpStatus().value();
            errorBody.put("errorCode", craftkitException.getErrorCode().getCode());
            errorBody.put("errorMsg", craftkitException.getMessage());
            errorBody.put("errorDetail", throwable.toString());
            messageCode = craftkitException.getErrorCode().getMessageKey();
        } else {
            errorBody.put("errorCode", CommonConstants.FAILURE_CODE);
            errorBody.put("errorMsg", CommonConstants.FAILURE_MESSAGE);
            errorBody.put("errorDetail", getExceptionMessage(throwable));
            messageCode = throwable.getClass().getName();
        }

        if (httpStatus != null) {
            errorBody.put("httpStatus", httpStatus);
        }
        errorBody.put("messageCode", messageCode);
        
        log.debug("CraftkitErrorAttributes :: {}", errorBody);
        return errorBody;
    }

    private String getStatus(RequestAttributes requestAttributes) {
        Integer status = getAttribute(requestAttributes, RequestDispatcher.ERROR_STATUS_CODE);
        if (status == null) {
            return UNKNOWN;
        }
        return status.toString();
    }

    private String getMessage(String status) {
        if (UNKNOWN.equals(status)) {
            return UNKNOWN;
        }
        return Objects.requireNonNull(HttpStatus.resolve(Integer.parseInt(status))).getReasonPhrase();
    }

    private String getDetail(String message, RequestAttributes requestAttributes) {
        String uri = getAttribute(requestAttributes, RequestDispatcher.ERROR_REQUEST_URI);
        if (StringUtils.isBlank(uri)) {
            return message;
        }
        return message + " at " + uri;
    }

    private String getExceptionMessage(Throwable th) {
        StringBuilder messages = new StringBuilder();
        Throwable cur = th;
        while (cur != null) {
            if (!messages.isEmpty()) {
                messages.append(" <- ");
            }
            messages.append(cur.toString());
            Throwable cause = cur.getCause();
            if (cause == cur) {
                break;
            }
            cur = cause;
        }
        return messages.toString();
    }

    @SuppressWarnings("unchecked")
    private <T> T getAttribute(RequestAttributes requestAttributes, String name) {
        return (T) requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
    }
}
