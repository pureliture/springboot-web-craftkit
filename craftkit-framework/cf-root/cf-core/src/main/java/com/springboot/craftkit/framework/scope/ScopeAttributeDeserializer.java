package com.springboot.craftkit.framework.scope;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.craftkit.framework.jwt.Claims;
import com.springboot.craftkit.framework.jwt.JwtType;
import com.springboot.craftkit.framework.util.HttpUtil;
import com.springboot.craftkit.framework.util.PropertyUtil;
import com.springboot.craftkit.framework.util.StringUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.springboot.craftkit.framework.scope.RequestScopeAttribute.*;


@Slf4j
public class ScopeAttributeDeserializer {

    public ScopeAttribute scopeAttribute;
    protected static final ObjectMapper objectMapper = new ObjectMapper();

    public ScopeAttributeDeserializer() {
        this.scopeAttribute = (ScopeAttribute) RequestScopeUtil.getAttribute();
    }


    public void setScopeAttributeByHttpHeader(HttpServletRequest request) {

        // 헤더 유무와 헤더 값유무를 체크하기 위해 map 으로 추출한다.
        Map<String, String> headers = new LinkedCaseInsensitiveMap<>();
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            headers.put(name, request.getHeader(name));
        }

        setHeaderByHttpAndKafka(headers::containsKey, headers::get);
        scopeAttribute.setForwardedService(request.getHeaders(FORWARDED_SERVICE), PropertyUtil.getApplicationName());
    }


    public void setScopeAttributeByKafkaHeader(Map<String, String> maps) {

        // case insensitive
        Map<String, String> headers = new LinkedCaseInsensitiveMap<>();
        headers.putAll(maps);

        setHeaderByHttpAndKafka(headers::containsKey, headers::get);

        // gtid 보정
        generateGtidIfNull(headers);

        // program_id 보정
        setScopeAttribute(scopeAttribute::setProgramId, scopeAttribute::getProgramId, "DESTINATION", headers::get);
        checkPgmIdLength();

        // forwarded-service가 없다면, application-name 으로 생성한다.
        String forwardedService = StringUtils.defaultIfBlank(headers.get(FORWARDED_SERVICE),
                StringUtils.defaultString(headers.get(APPLICATION_NAME)));

        scopeAttribute.setForwardedService(forwardedService, "message", PropertyUtil.getApplicationName());

        log.debug("RequestScopeAttribute deserialized by message {}", scopeAttribute);
    }

    private void generateGtidIfNull(Map<String, String> headers) {
        String gtid = scopeAttribute.getGtid();
        if (StringUtils.isNotBlank(gtid))
            return;

        gtid = headers.get("ID");
        if (StringUtils.isNotBlank(gtid)) {
            scopeAttribute.setGtid(gtid);
            return;
        }

        // MessageAttributeUTIL 에도 있음.
        scopeAttribute.setGtid(HttpUtil.generateNewGtid());
    }

    private void checkPgmIdLength() {
        if (scopeAttribute.getProgramId() != null && scopeAttribute.getProgramId().length() > 49) {
            String pgmId = scopeAttribute.getProgramId();
            scopeAttribute.setProgramId(pgmId.substring(pgmId.length() - 49, pgmId.length()));
        }
    }

    public Claims setScopeAttributeByAccessToken(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return null;

        String token = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(JwtType.ACCESS_TOKEN.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (StringUtils.isBlank(token))
            return null;

        Claims sfClaims = Claims.parse(token);

        scopeAttribute.setUserId(sfClaims.getUserId());
        scopeAttribute.setClientIp(sfClaims.getClientIp());
        scopeAttribute.setPrivateClaims(sfClaims.getPrivateClaims());

        return sfClaims;
    }

    /**
     * http요청에서 사용자ID, 프로그램ID, IP, x-async-to-sync 정보등을 보정한다.
     */
    public void updateScopeAttributeByHttpQuery(HttpServletRequest request) {

        setUserIdByQuery(request);
        setPgmIdByQuery(request);
        setClientIpByHttpRequest(request);
    }

    public ScopeAttribute getScopeAttribute() {
        return this.scopeAttribute;
    }

    private void setUserIdByQuery(HttpServletRequest request) {

        if (StringUtils.isNotEmpty(scopeAttribute.getUserId()))
            return;

        if (setScopeAttribute(scopeAttribute::setUserId, scopeAttribute::getUserId, "nextOperatorId", request::getParameter))
            if (setScopeAttribute(scopeAttribute::setUserId, scopeAttribute::getUserId, "_operatorId", request::getParameter))
                setScopeAttribute(scopeAttribute::setUserId, scopeAttribute::getUserId, "operatorId", request::getParameter);
    }

    private void setPgmIdByQuery(HttpServletRequest request) {

        if (StringUtils.isNotEmpty(scopeAttribute.getProgramId()))
            return;

        setScopeAttribute(scopeAttribute::setProgramId, scopeAttribute::getProgramId, "applicationId", request::getParameter);
    }

    private void setHeaderByHttpAndKafka(Predicate<String> headerChecker, UnaryOperator<String> headerGetter) {

        setScopeAttribute(scopeAttribute::setGtid, scopeAttribute::getGtid, GLOBAL_TRANSACTION_ID, headerGetter);
        setScopeAttribute(scopeAttribute::setUserId, scopeAttribute::getUserId, USER_ID, headerGetter);
        setScopeAttribute(scopeAttribute::setApplicationName, scopeAttribute::getApplicationName, APPLICATION_NAME, headerGetter);
        setScopeAttribute(scopeAttribute::setProgramId, scopeAttribute::getProgramId, PROGRAM_ID, headerGetter);
        setScopeAttribute(scopeAttribute::setPrivateClaims, scopeAttribute::getPrivateClaims, PRIVATE_CLAIMS, headerGetter);
        setScopeAttribute(scopeAttribute::setCustomAttributes, scopeAttribute::getCustomAttributes, CUSTOM_ATTRIBUTES, headerGetter);
        setScopeAttribute(scopeAttribute::setLogLevel, scopeAttribute::getLogLevel, LOG_LEVEL, headerGetter);

        /*
         * customAttribute 의 속성중 Http/eventuate 헤더에 직접 표현하는 속성(X-Custom-Attributes에 넣지 않고) 처리
         * 헤더 root 로 들어온 헤더명에서 복사해서 넣어준다. 단, customAttribute 와 헤더 root 와 동시에 있다면
         * 헤더 root 에 있는 값으로 덮어쓴다.
         * - X-MRKT-ID, X-REQ-HEADER 포함
         */
        ExposedCustomAttributeNames.getAttributes().forEach(name -> {
            // header 에 포함되어 있는 경우 처리
            if (headerChecker.test(name)) {

                // X-Async-To-Sync 가 헤더는 있고 값이 없이 들어올수 있음. 이때 변환 처리함.
                UnaryOperator<String> valueGetter = headerGetter;
                setScopeAttribute(value -> scopeAttribute.setCustomAttribute(name, value),
                        () -> null, // 항상 덮어쓴다.
                        name,
                        headerGetter);
            }
        });
    }

    private boolean setScopeAttribute(Consumer<String> attrSetter, Supplier<Object> attrVerifier, String headerName,
                                      UnaryOperator<String> headerGetter) {

        // 값이 이미 설정되었으면 skip
        Object obj = attrVerifier.get();
        if (obj instanceof Map && MapUtils.isNotEmpty((Map) obj)) {
            return false;
        }
        else if (obj != null && !(obj instanceof Map) && StringUtils.isNotBlank(obj.toString())
                && !StringUtils.equalsIgnoreCase(obj.toString(), RequestScopeAttribute.EMPTY)) {
            return false;
        }

        // 설정할 값이 없으면 skip
        String value = StringUtil.encodeURIComponent(headerGetter.apply(headerName));
        if (StringUtils.isBlank(value))
            return false;

        attrSetter.accept(value);

        log.trace("RequestScopeAttribute property updated {} : {}", headerName, value);
        return true;
    }

    private void setClientIpByHttpRequest(HttpServletRequest request) {

        try {
            // 빈값이 아니라면
            String ip = scopeAttribute.getClientIp();
            if (StringUtils.isNotBlank(ip)
                    && !StringUtils.equalsIgnoreCase(ip, RequestScopeAttribute.EMPTY)
                    && !StringUtils.equalsIgnoreCase(ip, RequestScopeAttribute.UNDEFINED_IP)) {
                return;
            }

            scopeAttribute.setClientIp(HttpUtil.getClientIp(request));
        } catch (Exception e) {
            log.error("IP체크 및 설정 오류", e);
        }
    }
}
