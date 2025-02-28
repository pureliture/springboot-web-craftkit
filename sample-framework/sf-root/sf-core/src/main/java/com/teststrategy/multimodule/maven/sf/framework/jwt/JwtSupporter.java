package com.teststrategy.multimodule.maven.sf.framework.jwt;

import com.teststrategy.multimodule.maven.sf.framework.application.constant.HttpRequestConstant;
import com.teststrategy.multimodule.maven.sf.framework.util.HttpUtil;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class JwtSupporter {

    /**
     *  sample-framework.tokens.[token].cookie-path 로 부터 "X-BFF-Context-Path" 또는 "{X-BFF-Context-Path}"를 추출하는 패턴변수
     */
    static final Pattern bffContestHeaderPattern =
            Pattern.compile("[{]?" + HttpRequestConstant.HTTP_HEADER_BFF_CONTEXT_PATH + "[}]?",
                    Pattern.CASE_INSENSITIVE);

    private JwtSupporter() {
        ;
    }

    /**
     * 입력문자열에서 {X-Bff-Context-Path} 변수를 헤더에서 추출하여 치환한다.
     *
     * @param property  {X-Bff-Context-Path} 변수를 포함하는 문자열
     * @return  {X-Bff-Context-Path} 를 헤더값에서 추출하여 치환한 문자열
     */
    public static String expendBffContextRootPath(String property) {
        try {
            if (StringUtils.containsIgnoreCase(property, HttpRequestConstant.HTTP_HEADER_BFF_CONTEXT_PATH)) {
                String headerValue =
                        HttpUtil.getCurrentRequestHeaderString(HttpRequestConstant.HTTP_HEADER_BFF_CONTEXT_PATH);
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
