package com.springboot.craftkit.framework.response;

import com.springboot.craftkit.framework.application.constant.CommonConstants;
import com.springboot.craftkit.framework.exception.model.CraftkitBaseException;
import com.springboot.craftkit.framework.util.MessageUtil;
import com.springboot.craftkit.framework.util.StringUtil;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 * ServerMessage utility for creating server response messages.
 */
public class ServerMessageUtil {

    private ServerMessageUtil() {
        super();
    }

    public static final String CODE_KEY = "code";
    public static final String MESSAGE_CODE_KEY = "messageCode";
    public static final String MESSAGE_KEY = "message";
    public static final int SERVER_MESSAGE_MAX_MESSAGE_LENGTH = 100;

    /**
     * Creates a server message string with code and message.
     *
     * @param code response code
     * @param message response message
     * @return URL encoded JSON string containing code and message
     */
    public static String getServerMessageString(String code, String message) {

        JSONObject jsonObj = new JSONObject();
        jsonObj.appendField(CODE_KEY, code);
        jsonObj.appendField(MESSAGE_KEY, message);

        String jsonStr;
        try {
            jsonStr = StringUtil.encodeURIComponent(jsonObj.toJSONString());
        } catch (Exception e) {
            jsonStr = jsonObj.toJSONString();
        }

        return jsonStr;
    }

    /**
     * Creates a server message string with code, messageCode, and message.
     *
     * @param code response code
     * @param messageCode message code for identification
     * @param message response message
     * @return URL encoded JSON string containing code, messageCode and message
     */
    public static String getServerMessageString(String code, String messageCode, String message) {

        if (StringUtils.length(message) > SERVER_MESSAGE_MAX_MESSAGE_LENGTH) {
            message = StringUtils.substring(message, 0, SERVER_MESSAGE_MAX_MESSAGE_LENGTH) + "...";
        }

        JSONObject jsonObj = new JSONObject();
        jsonObj.appendField(CODE_KEY, code);
        jsonObj.appendField(MESSAGE_CODE_KEY, messageCode);
        jsonObj.appendField(MESSAGE_KEY, message);

        String jsonStr;
        try {
            jsonStr = StringUtil.encodeURIComponent(jsonObj.toJSONString());
        } catch (Exception e) {
            jsonStr = jsonObj.toJSONString();
        }

        return jsonStr;
    }

    /**
     * Creates a server message string from a CraftkitBaseException.
     *
     * @param e the exception
     * @return URL encoded JSON string containing code and message from the exception
     */
    public static <T extends CraftkitBaseException> String getServerMessageString(T e) {
        String code = String.valueOf(e.getErrorCode().getHttpStatus().value());
        String messageCode = e.getErrorCode().getCode();
        return getServerMessageString(code, messageCode, getExceptionMessageForUser(e));
    }

    /**
     * Gets the user-facing message from an exception.
     *
     * @param e the exception
     * @return user-facing message
     */
    public static String getExceptionMessageForUser(Exception e) {
        if (e instanceof CraftkitBaseException) {
            CraftkitBaseException craftkitException = (CraftkitBaseException) e;
            // For system-level exceptions, return generic message
            if (craftkitException.getErrorCode().getHttpStatus().is5xxServerError()) {
                return MessageUtil.getMessage(CommonConstants.FAILURE_MESSAGE);
            }
        }
        return e.getMessage();
    }

}
