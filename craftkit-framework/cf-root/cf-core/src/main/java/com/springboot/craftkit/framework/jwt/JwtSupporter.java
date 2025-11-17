package com.springboot.craftkit.framework.jwt;

import com.springboot.craftkit.framework.application.constant.HttpHeaderConstant;
import com.springboot.craftkit.framework.util.HttpUtil;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class JwtSupporter {

    /**
     *  sample-framework.tokens.[token].cookie-path 로 부터 "BFF-Context-Path" 또는 "{BFF-Context-Path}"를 추출하는 패턴변수
     */
    static final Pattern bffContestHeaderPattern =
            Pattern.compile("[{]?" + HttpHeaderConstant.HTTP_HEADER_BFF_CONTEXT_PATH + "[}]?",
                    Pattern.CASE_INSENSITIVE);

    private JwtSupporter() {
        ;
    }

    /**
     * 입력문자열에서 {Bff-Context-Path} 변수를 헤더에서 추출하여 치환한다.
     */
    public static String expendBffContextRootPath(String property) {
        try {
            if (StringUtils.containsIgnoreCase(property, HttpHeaderConstant.HTTP_HEADER_BFF_CONTEXT_PATH)) {
                String headerValue = HttpUtil.getCurrentRequestHeaderString(HttpHeaderConstant.HTTP_HEADER_BFF_CONTEXT_PATH);
                if (headerValue.isEmpty()) {
                    headerValue = "bff";
                }
                return RegExUtils.replaceFirst(property, JwtSupporter.bffContestHeaderPattern, headerValue).replace("//", "/");
            }
            return property;
        } catch (Exception e) {
            return property;
        }
    }
}
