package com.teststrategy.multimodule.maven.sf.framework.jwt;

import com.teststrategy.multimodule.maven.sf.framework.application.setting.JwtProperties;
import com.teststrategy.multimodule.maven.sf.framework.application.setting.JwtProperties.TokenProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalTime;
import java.util.Map;

@Slf4j
public enum JwtType {

    ACCESS_TOKEN    ("X-WAF-A-TOKEN", "A"), /* access token */
    REFRESH_TOKEN   ("X-WAF-R-TOKEN", "R"), /* refresh token */
    TEMPORARY_TOKEN ("X-WAF-T-TOKEN", "T"), /* 2 fact verifaction token */
    BYPASS_TOKEN    ("X-WAF-B-TOKEN", "B"), /* sso error bypass token */
    CUSTOM_TOKEN    ("X-WAF-C-TOKEN", "C"), /* coustom token : 아직 정해지지 않음 */
    UNDEFINED_TOKEN    ("X-WAF-UNDEFINED", "U");

    private String tokenName;
    private String prefix;

    private JwtType(String tokenName, String prefix) {
        this.tokenName = tokenName;
        this.prefix = prefix;
    }

    public String getName() {
        return tokenName;
    }

    public String getPrefix() {
        return prefix;
    }

    public static JwtType getTypeByJti(String jti) {
        if (StringUtils.isEmpty(jti))
            return UNDEFINED_TOKEN;

        switch (jti.charAt(0)) {
            case 'A':
                return ACCESS_TOKEN;
            case 'R':
                return REFRESH_TOKEN;
            case 'T':
                return TEMPORARY_TOKEN;
            case 'B':
                return BYPASS_TOKEN;
            case 'C':
                return CUSTOM_TOKEN;
            default:
                return UNDEFINED_TOKEN;
        }
    }

    public long getCookieMaxAge(JwtProperties jwtSetting) {
        TokenProperties token = getTokenProperties(jwtSetting);
        if (token == null)
            return 0;

        return token.getCookieMaxAge();
    }

    public LocalTime getNewDayTime(JwtProperties jwtSetting) {
        TokenProperties token = getTokenProperties(jwtSetting);
        if (token == null)
            return null;

        return token.getNewDayTime();
    }

    public long getTokenMaxAge(JwtProperties jwtSetting) {
        TokenProperties token = getTokenProperties(jwtSetting);
        if (token == null)
            return 31536000; // 365일

        Long maxAge = token.getTokenMaxAge();
        if (maxAge == null)
            return 31536000; // 365일

        return maxAge.longValue();
    }

    public Long[] getAgeRange(JwtProperties jwtSetting) {
        TokenProperties token = getTokenProperties(jwtSetting);
        Long[] nullValues = {};
        Long[] result = {0L, 0L};

        if (token == null)
            return nullValues;

        Long[] values = token.getTokenAgeRange();
        if (values == null || values.length == 0)
            return nullValues;

        if (values.length == 1) {
            result[0] = values[0];
            result[1] = values[0];
        } else {
            result[0] = values[0];
            result[1] = values[1];
        }

        if (values[0] < 0L) {
            result[0] = 0L;
        }

        if (values.length == 1 || values[1] < 0L) {
            result[1] = 0L;
        }

        if (values.length == 1 || !values[0].equals(result[0]) || !values[1].equals(result[1])) {
            log.debug("{} token의 Range 가 보정되었습니다. {} -> {}", this.name(), values, result);
            token.setTokenAgeRange(result);
        }

        return result;
    }

    /**
     * JWT properties sf.tokens.[token].cookie-path 의 값을 추출하여 반환하며, 속성값 중 "{X-BFF-Context-Path}" 를 가지고 있다면
     * 요청헤더에서 X-BFF-Context-Path를 추출하여 치환하는 처리를 수행한다.
     * <PRE>
     * 주의사항) property에 "{X-BFF-Context-Path}" 를 포함하였지만, 요청헤더에 값이 없다면 bffui를 기본값을 설정한다.
     * </PRE>
     * @param jwtSetting 어플리케이션 설정에 포함된 JWT설정(sf.tokens)을 로딩한 JwtProperties 속성
     * @return 속성 중 cookie-path를 추출한 결과
     */
    @SuppressWarnings("javadoc")
    public String getCookiePath(JwtProperties jwtSetting) {
        TokenProperties token = getTokenProperties(jwtSetting);
        if (token == null)
            return "/";

        return JwtSupporter.expendBffContextRootPath(token.getCookiePath());
    }

    private TokenProperties getTokenProperties(JwtProperties jwtSetting) {
        Map<JwtType, TokenProperties> tokens = jwtSetting.getTokens();
        if (tokens == null)
            return null;

        return tokens.get(this);
    }
}
