package com.springboot.craftkit.framework.response;

import com.springboot.craftkit.framework.application.constant.CommonConstants;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Objects;

/**
 * Utility class for setting server messages in HTTP response headers.
 */
@Slf4j
public class ServerMessageSupporter {

    private static final String SERVER_MESSAGE = CommonConstants.SERVER_MESSAGE;
    private static final String NO_CONTENT_CODE = CommonConstants.NO_CONTENT_CODE;
    private static final String NO_CONTENT_MESSAGE = CommonConstants.NO_CONTENT_MESSAGE;
    private static final String SUCCESS_CODE = CommonConstants.SUCCESS_CODE;
    private static final String SUCCESS_MESSAGE = CommonConstants.SUCCESS_MESSAGE;

    /**
     * Checks if serverMessage has already been set.
     *
     * @param response ServerHttpResponse
     * @return true if SERVER_MESSAGE header already exists
     */
    public static boolean hasSetServerMessage(ServerHttpResponse response) {
        return response.getHeaders().containsKey(SERVER_MESSAGE);
    }

    /**
     * Sets server message for empty body response (code = 204).
     *
     * @param response ServerHttpResponse
     * @return the response with server message header set
     */
    public static ServerHttpResponse setNoContentServerMessage(ServerHttpResponse response) {
        String serverMessage = ServerMessageUtil.getServerMessageString(NO_CONTENT_CODE, "success", NO_CONTENT_MESSAGE);
        response.getHeaders().set(SERVER_MESSAGE, serverMessage);
        return response;
    }

    /**
     * Sets server message for null content response (code = 204).
     *
     * @param response ServerHttpResponse
     * @return the response with server message header set
     */
    public static ServerHttpResponse setNullContentServerMessage(ServerHttpResponse response) {
        String serverMessage = ServerMessageUtil.getServerMessageString(NO_CONTENT_CODE, "success", NO_CONTENT_MESSAGE);
        response.getHeaders().set(SERVER_MESSAGE, serverMessage);
        setNoContentBody(response);
        return response;
    }

    /**
     * Handles empty body by writing empty bytes.
     * When body is null, headers may not be set properly, so body write is performed.
     *
     * @param response ServerHttpResponse
     */
    private static void setNoContentBody(ServerHttpResponse response) {
        try {
            response.getBody().write("".getBytes());
        } catch (IOException e) {
            // ignore
        }
    }

    /**
     * Sets success server message (code = 200).
     *
     * @param response ServerHttpResponse
     * @return the response with server message header set
     */
    public static ServerHttpResponse setSuccessServerMessage(ServerHttpResponse response) {
        String serverMessage = ServerMessageUtil.getServerMessageString(SUCCESS_CODE, "success", SUCCESS_MESSAGE);
        response.getHeaders().set(SERVER_MESSAGE, serverMessage);
        return response;
    }

    /**
     * Sets server message for partial success scenarios.
     *
     * @param code message code, typically 200
     * @param message message content from message.properties
     */
    public static void setServerMessageByBusiness(String code, String message) {
        String serverMessage = ServerMessageUtil.getServerMessageString(code, "success.partially", message);
        try {
            HttpServletResponse response =
                    ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
            Objects.requireNonNull(response).setHeader(SERVER_MESSAGE, serverMessage);
        } catch (Exception e) {
            log.debug("ServerMessageUtil.setServerMessage... getResponse error skip {}", e.getMessage());
        }
    }

}
