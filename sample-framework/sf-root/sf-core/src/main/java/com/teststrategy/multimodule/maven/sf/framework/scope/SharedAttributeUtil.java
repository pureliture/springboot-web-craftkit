package com.teststrategy.multimodule.maven.sf.framework.scope;


public class SharedAttributeUtil {

    private SharedAttributeUtil() {}

    /**
     * 공유속성을 등록한다.
     * 헤더 길이가 너무 길어, 요청에 실패가 발생하지 않도록, 키,값의 길이를 최대한 축약하여 사용한다.
     */
    public static void setAttribute(String key, String value) {
        ScopeAttribute scopeAttr = (ScopeAttribute) RequestScopeUtil.getAttribute();
        scopeAttr.setCustomAttribute(key, value);
    }

    /**
     * 공유속성을 조회한다.
     */
    public static String getAttribute(String key) {
        ScopeAttribute scopeAttr = (ScopeAttribute) RequestScopeUtil.getAttribute();
        return scopeAttr.getCustomAttribute(key).toString();
    }
}
