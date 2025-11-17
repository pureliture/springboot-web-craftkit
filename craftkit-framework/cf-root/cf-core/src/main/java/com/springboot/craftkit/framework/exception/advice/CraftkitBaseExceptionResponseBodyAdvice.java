package com.springboot.craftkit.framework.exception.advice;

import com.springboot.craftkit.framework.exception.model.CraftkitBaseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;


public interface CraftkitBaseExceptionResponseBodyAdvice
        extends CraftkitBaseExceptionAdvice, ResponseBodyAdvice<Object> {

    // TODO : default 메소드 내부 코드 구현 필요
    <T extends CraftkitBaseException> ResponseEntity<Object> handleCraftkitBaseException(T e);
}
