package com.teststrategy.multimodule.maven.sf.framework.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * <pre>
 *    JwtType tokenType : access-token, refresh-token, temporary-token(2fact인증용) 구분
 *    String intgUserId : 사용자 ID - jwt regisisterd clas "sub" 에 적용함
 *    List&lt;String&gt; groups : 그룹 - 생성된 token 이 허용되는 대상 - jwt regisisterd clas "aud" 에 적용함
 *    Map<String, String> privateClaims : 그외 사용자 정의 claim 등록
 * </pre>
 */
@Slf4j
public class Claims {

    /*
     * JWT 표준으로 정의된 registered claim
     * iss: 토큰 발급자 (issuer)
     * sub: 토큰 제목 (subject)
     * aud: 토큰 대상자 (audience)
     * exp: 토큰의 만료시간 (expiraton), 시간은 NumericDate 형식으로 되어있어야 하며 (예: 1480849147370) 언제나 현재
     *      시간보다 이후로 설정되어있어야합니다.
     * nbf: Not Before 를 의미하며, 토큰의 활성 날짜와 비슷한 개념입니다. 여기에도 NumericDate 형식으로 날짜를 지정하며,
     *      이 날짜가 지나기 전까지는 토큰이 처리되지 않습니다.
     * iat: 토큰이 발급된 시간 (issued at), 이 값을 사용하여 토큰의 age 가 얼마나 되었는지 판단 할 수 있습니다.
     * jti: JWT의 고유 식별자로서, 주로 중복적인 처리를 방지하기 위하여 사용됩니다. 일회용 토큰에 사용하면 유용합니다.
     */

    private static final String ISSUER_CLAIM = "iss";
    private static final String SUBJECT_CLAIM = "sub"; // intg-user-id
    private static final String AUDIENCE_CLAIM = "aud"; // groups
    private static final String EXPIRATION_TIME_CLAIM = "exp";
    private static final String NOT_BEFORE_CLAIM = "nbf";
    private static final String ISSUED_AT_CLAIM = "iat";
    private static final String JWT_ID_CLAIM = "jti";

    private static final String CLIENT_IP = "client-ip"; // 개인정보 로깅용 ip
    private static final String AUTHENTICATED_IP = "authenticated-ip"; // jwt인증용 ip
    private static final String USER_ID = "user-id"; // 사용자ID

    private static final Set<String> NOT_ALLOWED_PRIVATE_CLAIM_NAMES;

    /**
     * Initialize the registered claim name set.
     */
    static {
        Set<String> claimSet = Set.of(ISSUER_CLAIM, SUBJECT_CLAIM, AUDIENCE_CLAIM, EXPIRATION_TIME_CLAIM, NOT_BEFORE_CLAIM,
                ISSUED_AT_CLAIM, JWT_ID_CLAIM, CLIENT_IP, AUTHENTICATED_IP, USER_ID);

        NOT_ALLOWED_PRIVATE_CLAIM_NAMES = Collections.unmodifiableSet(claimSet);
    }

    /**
     * token type (access-token, refresh-token, temporary-token)
     */
    JwtType tokenType;

    private String jti;
    private String issuer;

    private String intgUserId; // sub

    private List<String> groups = new ArrayList<>(); // aud
    private ZonedDateTime issueAt = ZonedDateTime.now(); // iat
    private ZonedDateTime expireAt; // exp

    private String clientIp;
    private String authenticatedIp;
    private String userId;

    Map<String, Object> privateClaims = new LinkedHashMap<>();

    /**
     * 만료계산기로 만료일시를 반환
     */
    public ZonedDateTime getExpireAt(JwtExpCalculator calculator) {
        if (expireAt != null)
            return expireAt;

        this.expireAt = calculator.getExpireAt(tokenType, issueAt);
        return expireAt;
    }

    public Claims(JwtType tokenType) {
        super();
        this.jti = makeShortUUID(tokenType.getPrefix());
        this.tokenType = tokenType;
    }

    public Claims(JwtType tokenType, String userId) {
        super();
        this.jti = makeShortUUID(tokenType.getPrefix());
        this.tokenType = tokenType;
        this.userId = userId;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Claims parse(String token) {

        String[] parts = token.split("\\.");
        if (parts.length < 2)
            return null;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            byte[] bytes = Base64.getDecoder().decode(urlSafeCharToStandardChar(parts[1]));
            HashMap payload = objectMapper.readValue(bytes, HashMap.class);

            JwtType jwtType = JwtType.valueOfJti((String) payload.get(JWT_ID_CLAIM));
            return new Claims(jwtType, (String) payload.get(USER_ID))
                            .addGroup(payload.get(AUDIENCE_CLAIM))
                            .setJti((String) payload.get(JWT_ID_CLAIM))
                            .setIssuer((String) payload.get(ISSUER_CLAIM))
                            .setIssueAt(ZonedDateTime.ofInstant(
                                    Instant.ofEpochMilli(((int) payload.get(ISSUED_AT_CLAIM)) * 1000L),
                                    ZoneId.systemDefault()))
                            .setExpireAt(ZonedDateTime.ofInstant(
                                    Instant.ofEpochMilli(((int) payload.get(EXPIRATION_TIME_CLAIM)) * 1000L),
                                    ZoneId.systemDefault()))
                            .setClientIp((String) payload.get(CLIENT_IP))
                            .setAuthenticatedIp((String) payload.get(AUTHENTICATED_IP))
                            .setPrivateClaims(payload);

        } catch (Exception e) {
            log.error("Claims.parse() exception : {}", e.getMessage());
            return null;
        }
    }

    private static String urlSafeCharToStandardChar(String base64UrlSafeString) {
        // Supports '+' and '/' for standard base 64, but also '-' and '_' for
        return StringUtils.replaceEach(base64UrlSafeString, new String[] {"-","_"}, new String[] {"+","/"});
    }


    /**
     * group 을 추가한다.
     * <PRE>
     * 추가된 그룹은 jwt regisisterd clas "aud"  에 등록된다.
     * </PRE>
     */
    public Claims addGroup(Object... groups) {
        if (groups[0] != null)
            Arrays.asList(groups).forEach(group -> this.groups.add(group.toString()));
        return this;
    }

    /**
     * private claim을 등록한다.
     * <PRE>
     * registerd claim 외 사용자 정의 클레임을 추가힌다.
     * </PRE>
     */
    public Claims addPrivateClaim(String key, Object value) {
        privateClaims.put(key, value);
        return this;
    }

    private String makeShortUUID(String prefix) {
        UUID uuid = UUID.randomUUID();
        long l = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
        String str = Long.toString(l, Character.MAX_RADIX);
        return prefix + str;
    }

    /**
     * @return the tokenType
     */
    public JwtType getTokenType() {
        return tokenType;
    }

    public boolean isTypeOf(JwtType type) {
        return tokenType == type;
    }

    /**
     * @return the jti
     */
    public String getJti() {
        return jti;
    }

    /**
     * @param jti the jti to set
     */
    public Claims setJti(String jti) {
        if (jti != null)
            this.jti = jti;
        return this;
    }

    /**
     * @return the issuer
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * @param issuer the issuer to set
     */
    public Claims setIssuer(String issuer) {
        if (issuer != null)
            this.issuer = issuer;
        return this;
    }

    /**
     * @return the groups
     */
    public List<String> getGroups() {
        return groups;
    }

    /**
     * @param groups the groups to set
     */
    public Claims setGroups(List<String> groups) {
        if (groups != null)
            this.groups = groups;
        return this;
    }

    /**
     * @return the issueAt
     */
    public ZonedDateTime getIssueAt() {
        return issueAt;
    }

    /**
     * @param issueAt the issueAt to set
     */
    public Claims setIssueAt(ZonedDateTime issueAt) {
        if (issueAt != null)
            this.issueAt = issueAt;
        return this;
    }

    /**
     * @return the expireAt
     */
    public ZonedDateTime getExpireAt() {
        return expireAt;
    }

    /**
     * @param expireAt the expireAt to set
     */
    public Claims setExpireAt(ZonedDateTime expireAt) {
        if (expireAt != null)
            this.expireAt = expireAt;
        return this;
    }

    /**
     * @return the privateClaims
     */
    public Map<String, Object> getPrivateClaims() {
        return privateClaims;
    }

    /**
     * 기본 registered claim 외 범용으로 사용할 claim의 collection.
     */
    public Map<String, Object> getPreFixedPrivateClaims() {
        Map<String, Object> claims = new LinkedHashMap<>();

        //U+ 에서 범용으로 사용할 목적으로 예약한 private claims
        claims.put(CLIENT_IP, clientIp);
        claims.put(AUTHENTICATED_IP, authenticatedIp);
        claims.put(USER_ID, userId);

        return claims;
    }

    /**
     * @param privateClaims the privateClaims to set
     */
    public Claims setPrivateClaims(Map<String, Object> privateClaims) {

        if (privateClaims != null)
            privateClaims.forEach((key, value) -> {
                if (!NOT_ALLOWED_PRIVATE_CLAIM_NAMES.contains(key))
                    this.privateClaims.put(key, value);
            });

        return this;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @return the clientIp
     */
    public String getClientIp() {
        return clientIp;
    }

    /**
     * @param clientIp the clientIp to set
     */
    public Claims setClientIp(String clientIp) {
        this.clientIp = clientIp;
        return this;
    }

    /**
     * @return the authenticatedIp
     */
    public String getAuthenticatedIp() {
        return authenticatedIp;
    }

    /**
     * @param authenticatedIp the authenticatedIp to set
     */
    public Claims setAuthenticatedIp(String authenticatedIp) {
        this.authenticatedIp = authenticatedIp;
        return this;
    }

    @Override
    public String toString() {
        // @formatter:off
        return String.format(
                "Claims " + "[tokenType=%s, jti=%s, issuer=%s, userId=%s, groups=%s, issueAt=%s, expireAt=%s, clientIp=%s, authenticatedIp=%s, privateClaims=%s]",
                tokenType, jti, issuer, userId, groups, issueAt, expireAt, clientIp, authenticatedIp, privateClaims);
        // @formatter:on
    }
}
