package com.springboot.craftkit.framework.response.advice;

import com.springboot.craftkit.framework.response.ServerMessageSupporter;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Default response advice for setting server messages.
 * <pre>
 *  1. If body is null, sets server_message.code = "204"
 *  2. If body is empty string, sets server_message.code = "204"
 *  3. Otherwise sets server_message.code = "200"
 * </pre>
 */
@ControllerAdvice
@Order(100) // Common response order - should run after exception handlers
public class CommonResponseAdvice<T> implements ResponseBodyAdvice<T> {

    @Override
    public boolean supports(@NonNull MethodParameter returnType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * Sets default response server message headers.
     */
    @Override
    public T beforeBodyWrite(T body, @NonNull MethodParameter returnType, @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType, @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response) {
        setCommonServerMessageHeader(body, response);
        return body;
    }

    /**
     * Sets default server message in the response header.
     */
    @SuppressWarnings("UnusedReturnValue")
    private ServerHttpResponse setCommonServerMessageHeader(T body, ServerHttpResponse response) {
        if (!ServerMessageSupporter.hasSetServerMessage(response)) {
            if (body == null) {
                return ServerMessageSupporter.setNullContentServerMessage(response);
            } else if ("".equals(body)) {
                return ServerMessageSupporter.setNoContentServerMessage(response);
            } else {
                return ServerMessageSupporter.setSuccessServerMessage(response);
            }
        }
        return response;
    }

}
