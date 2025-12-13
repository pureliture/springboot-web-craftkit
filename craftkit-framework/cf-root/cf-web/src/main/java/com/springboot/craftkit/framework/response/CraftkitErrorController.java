/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springboot.craftkit.framework.response;

import com.springboot.craftkit.framework.application.constant.CommonConstants;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * Basic global error {@link Controller @Controller}, rendering {@link ErrorAttributes}.
 * More specific errors can be handled either using Spring MVC abstractions (e.g.
 * {@code @ExceptionHandler}) or by adding servlet
 * {@link org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory#setErrorPages server error pages}.
 *
 * @see ErrorAttributes
 * @see ErrorProperties
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class CraftkitErrorController extends BasicErrorController {

    public static final String HTTP_HEADER_BIZ_ERROR = "BIZError";

    public CraftkitErrorController(ErrorAttributes errorAttributes, ErrorProperties errorProperties, List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, errorProperties, errorViewResolvers);
    }

    public CraftkitErrorController(ErrorAttributes errorAttributes, ErrorProperties errorProperties) {
        super(errorAttributes, errorProperties);
    }


    @Override
    @RequestMapping
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        if (status == HttpStatus.NO_CONTENT) {
            return new ResponseEntity<>(status);
        }
        Map<String, Object> body = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));

        // Extract httpStatus from CraftkitErrorAttributes if present
        final String HTTP_STATUS = "httpStatus";
        if (body.containsKey(HTTP_STATUS)) {
            int httpStatusInt = (int) body.get(HTTP_STATUS);
            body.remove(HTTP_STATUS);
            status = HttpStatus.valueOf(httpStatusInt);
        }

        final String MESSAGE_CODE = "messageCode";
        String messageCode = "undefined";
        if (body.containsKey(MESSAGE_CODE)) {
            messageCode = body.get(MESSAGE_CODE).toString();
            body.remove(MESSAGE_CODE);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set(
                HTTP_HEADER_BIZ_ERROR,
                status.is2xxSuccessful() ? "Y" : "N");
        headers.set(
                CommonConstants.SERVER_MESSAGE,
                ServerMessageUtil.getServerMessageString(
                        body.get("errorCode").toString(),
                        messageCode,
                        body.get("errorMsg").toString()));

        return new ResponseEntity<>(body, headers, status);
    }

    /**
     * Handles HttpMediaTypeNotAcceptableException.
     */
    @Override
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<String> mediaTypeNotAcceptable(HttpServletRequest request) {
        HttpStatus status;
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (statusCode == null || statusCode == 500) {
            status = HttpStatus.NOT_ACCEPTABLE;
        } else {
            try {
                status = HttpStatus.valueOf(statusCode);
            } catch (Exception ex) {
                status = HttpStatus.NOT_ACCEPTABLE;
            }
        }
        
        return ResponseEntity.status(status).build();
    }
}
