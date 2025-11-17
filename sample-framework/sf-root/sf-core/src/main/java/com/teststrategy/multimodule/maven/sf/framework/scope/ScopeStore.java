package com.teststrategy.multimodule.maven.sf.framework.scope;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Slf4j
public class ScopeStore implements RequestScopeStore {

    private static final ThreadLocal<RequestScopeAttribute> attributeThreadLocal = new ThreadLocal<>();

    /*==================================================================================================================
    *
    * Request scope(또는 thread local) 조회 및 설정
    *
    ==================================================================================================================*/
    @Override
    public RequestScopeAttribute getAttribute() {

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        // async 설정용(asyns task, hystrix, kafka)
        if (requestAttributes == null) {

            RequestScopeAttribute attribute = attributeThreadLocal.get();
            if (attribute == null) {
                attribute = emptyAttribute();
                return setAndGetThreadLocal((ScopeAttribute) attribute);
            }
            return attribute;
        }

        // http
        RequestScopeAttribute attribute =
                (RequestScopeAttribute) requestAttributes.getAttribute(ScopeAttribute.KEY, RequestAttributes.SCOPE_REQUEST);

        if (attribute == null) {
            attribute = emptyAttribute();
            requestAttributes.setAttribute(ScopeAttribute.KEY, attribute, RequestAttributes.SCOPE_REQUEST);
            return attribute;
        }

        return attribute;
    }

    /*==================================================================================================================
     *
     * async 설정용(asyns task, hystrix, kafka)
     *
     ==================================================================================================================*/
    public void setAttributeIntoThreadLocal(ScopeAttribute attribute) {
        setAndGetThreadLocal(attribute);
    }

    private ScopeAttribute setAndGetThreadLocal(ScopeAttribute attribute) {
        // 기존: (ScopeAttribute) attribute.clone();  -> S2975 위반
        ScopeAttribute cloned = ScopeAttribute.of(attribute); // 복사 생성자 기반 복제
        attributeThreadLocal.set(cloned);
        return cloned;
    }

    public void removeAttributeFromThreadLocal() {
        attributeThreadLocal.remove();
    }

    private RequestScopeAttribute emptyAttribute() {
        return new ScopeAttribute();
    }
}