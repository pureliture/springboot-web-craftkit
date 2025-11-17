package com.springboot.craftkit.framework.jwt;

import com.springboot.craftkit.framework.application.setting.JwtProperties;
import com.springboot.craftkit.framework.application.setting.JwtProperties.TokenProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public enum JwtType {

    ACCESS_TOKEN    ("X-WAF-A-TOKEN", "A"), /* access token */
    REFRESH_TOKEN   ("X-WAF-R-TOKEN", "R"), /* refresh token */
    TEMPORARY_TOKEN ("X-WAF-T-TOKEN", "T"), /* 2 fact verifaction token */
    BYPASS_TOKEN    ("X-WAF-B-TOKEN", "B"), /* sso error bypass token */
    CUSTOM_TOKEN    ("X-WAF-C-TOKEN", "C"), /* coustom token : 아직 정해지지 않음 */
    UNDEFINED_TOKEN    ("X-WAF-UNDEFINED", "U");

    private static final long DEFAULT_TOKEN_MAX_AGE = 31_536_000L; // 365일
    private static final Map<Character, JwtType> TOKEN_MAP = new HashMap<>();

    static {
        for (JwtType type : values()) {
            TOKEN_MAP.put(type.prefix.charAt(0), type);
        }
    }

    private String tokenName;
    @Getter
    private String prefix;

    JwtType(String tokenName, String prefix) {
        this.tokenName = tokenName;
        this.prefix = prefix;
    }

    public String getName() {
        return tokenName;
    }

    public static JwtType valueOfJti(String jti) {
        if (StringUtils.isEmpty(jti)) {
            return UNDEFINED_TOKEN;
        }
        return TOKEN_MAP.getOrDefault(jti.charAt(0), UNDEFINED_TOKEN);
    }

    public long getCookieMaxAge(JwtProperties jwtSetting) {
        return getTokenProperties(jwtSetting)
                .map(TokenProperties::getCookieMaxAge)
                .orElse(0L);
    }

    public LocalTime getNewDayTime(JwtProperties jwtSetting) {
        return getTokenProperties(jwtSetting)
                .map(TokenProperties::getNewDayTime)
                .orElse(null);
    }

    public long getTokenMaxAge(JwtProperties jwtSetting) {
        return getTokenProperties(jwtSetting)
                .map(TokenProperties::getTokenMaxAge)
                .orElse(DEFAULT_TOKEN_MAX_AGE);
    }

    public Long[] getAgeRange(JwtProperties jwtSetting) {
        return getTokenProperties(jwtSetting)
                .map(TokenProperties::getTokenAgeRange)
                .filter(values -> values.length > 0)
                .map(values -> {
                    Long min = Math.max(values[0], 0L);
                    Long max = (values.length > 1) ? Math.max(values[1], 0L) : min;
                    Long[] result = {min, max};

                    if (!values[0].equals(min) || (values.length > 1 && !values[1].equals(max))) {
                        log.debug("{} token의 Range가 보정되었습니다. {} -> {}", this.name(), values, result);
                    }
                    return result;
                })
                .orElse(new Long[]{});
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
        return getTokenProperties(jwtSetting)
                .map(TokenProperties::getCookiePath)
                .map(JwtSupporter::expendBffContextRootPath)
                .orElse("/");
    }

    private Optional<TokenProperties> getTokenProperties(JwtProperties jwtSetting) {
        return Optional.ofNullable(jwtSetting.getTokens())
                .map(tokens -> tokens.get(this));
    }

    @Override
    public String toString() {
        return String.format("JwtType{name='%s', prefix='%s'}", tokenName, prefix);
    }
}
