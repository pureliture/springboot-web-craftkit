package com.springboot.craftkit.framework.filter;

import com.springboot.craftkit.framework.application.setting.CorsFilterProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * CORS(Cross-Origin Resource Sharing) 허용처리 Filter
 *
 * <PRE>
 * [Spring boot Autoconfiguration]
 * {@link com.springboot.craftkit.config.web.WebConfig#corsFilterRegistrationBean(CorsFilterProperties)()}
 *
 * [application.yml 설정]
 * craftkit.cors-filter.enabled == true 일때 Filter 적용됨
 * craftkit.cors-filter 는 enabled 외에 아래와 같은 속성을 가진다.
 *   access-control-allow-origin : 기본값 *
 *     1. cors 를 허용할 origin 정보
 *     2. wildcard 로 * 를 허용한다.
 *     3. 만약 HTTP Cookie 와 HTTP Authentication 헤더를 포함해야 하는 경우, wildcard 가 허용되지 않으므로,
 *        내부적으로 요청의 Origin 값을 재설정한다.
 *   access-Control-allow-credentials: 기본값 false
 *     1.  HTTP Cookie 와 HTTP Authentication 를 사용하기 위해서 true 를 설정한다.
 *   access-control-allow-headers : 기본값 *
 *     1. CORS preflight(OPTIONS 메소드)의 응답으로 Access-Control-Allow-Headers 응답 값
 *     2. HTTP Cookie 와 HTTP Authentication 를 포함하면 * (wildcard) 사용 불가하므로,
 *        Access-Control-Request-Headers 를 재설정하여 응답한다.
 *   access-control-allow-methods: 기본값 "GET, POST, PUT, DELETE"
 *     1. CORS preflight(OPTIONS 메소드)의 응답으로 Access-Control-Allow-Method 응답 값
 *   access-control-max-age: 기본값 1
 *     1. cors 를 허용하는 시간
 *</PRE>
 */
@Slf4j
@NoArgsConstructor
public class CorsFilter extends OncePerRequestFilter {

    String allowOrigin = "*";
    Boolean allowCredentials = false;
    String allowMethods = "GET, POST, PUT, DELETE";
    String allowHeaders = null;
    int maxAge = 1;

    /**
     * @param allowOrigin  application.yml 에 설정된 craftkit.cors-filter.access-control-allow-origin 값 또는 사용자정의 값
     * @param allowCredentials application.yml 에 설정된 craftkit.cors-filter.access-control-allow-credentials 값 또는 사용자정의 값
     * @param allowMethods application.yml 에 설정된 craftkit.cors-filter.access-control-allow-methods 값 또는 사용자정의 값
     * @param allowHeaders application.yml 에 설정된 craftkit.cors-filter.access-control-allow-headers 값 또는 사용자정의 값
     * @param maxAge application.yml 에 설정된 craftkit.cors-filter.access-control-max-age 값 또는 사용자정의 값
     */
    public CorsFilter(String allowOrigin, boolean allowCredentials, String allowMethods, String allowHeaders,
            int maxAge) {
        super();

        if (allowOrigin != null && !allowOrigin.isEmpty())
            this.allowOrigin = allowOrigin;

        this.allowCredentials = allowCredentials;

        if (allowMethods != null && !allowMethods.isEmpty())
            this.allowMethods = allowMethods;

        if (allowHeaders != null && !allowHeaders.isEmpty())
            this.allowHeaders = allowHeaders;
        else
            this.allowHeaders = "*";

        if (maxAge > 0)
            this.maxAge = maxAge;
    }

    /**
     *  CORS(Cross-Origin Resource Sharing) 허용처리 Filter 구현 method
     *
     * @param request HttpServletRequest 형식의 Http 요청
     * @param response HttpServletResponse 형식의 Http 응답
     * @param filterChain FilterChain 형식으로 FilterChain.doFilter()을 호출하여 정상작업 수행할 수 있음
     * @see org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, jakarta.servlet.FilterChain)
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        response.addHeader("Access-Control-Allow-Credentials", allowCredentials.toString());
        response.addHeader("Access-Control-Allow-Origin", getOrigin());

        if (request.getHeader("Access-Control-Request-Method") != null && "OPTIONS".equals(request.getMethod())) {

            log.trace("CORS preflight {}", request.getRequestURI());
            String requestHeaders = getRequestHeaders(request);

            response.addHeader("Access-Control-Allow-Methods", allowMethods);
            response.addHeader("Access-Control-Allow-Headers", requestHeaders);
            response.addHeader("Access-Control-Max-Age", Integer.valueOf(maxAge).toString());
        }

        filterChain.doFilter(request, response);

    }

    /**
     * CORS 요청의 응답 중 Access-Control-Allow-Origin 의 응답 값 생성
     * <PRE>
     * 1. 기본값 * 또는 craftkit.cors-filter.access-control-allow-origin 값 설정
     * 2. HTTP Cookie 와 HTTP Authentication 를 포함하는 경우 wildcard(*) 를 허용하지 않음을 주의하여야 한다.
     * </PRE>
     * @return http 응답 Header 로 Access-Control-Allow-Origin 에 설정한 문자열
     */
    private String getOrigin() {
        String origin = allowOrigin;
        if (allowCredentials && "*".equals(allowOrigin)) {
            log.warn("HTTP Cookie 와 HTTP Authentication 를 포함요청(Access-Control-Allow-Credentials == true)은"
                    + " Access-Control-Allow-Origin 에 *(wildcard)를 허용하지 않습니다.");
        }
        return origin;
    }

    /**
     * CORS 요청의 응답 중 Access-Control-Allow-Headers 의 응답 값 생성
     * <PRE>
     * 1. 기본값 * 또는 craftkit.cors-filter.access-control-allow-headers 값 설정
     * 2. CORS preflight(OPTIONS 메소드)요청에서 사용된다.
     * 3. HTTP Cookie 와 HTTP Authentication 를 포함하면 * (wildcard) 사용 불가하므로,
     *    Access-Control-Request-Headers 를 재설정하여 응답한다.
     * </PRE>
     * @param request HttpServletRequest
     * @return http 응답 Header 로 Access-Control-Request-Headers 에 설정할 응답 문자열
     */
    private String getRequestHeaders(HttpServletRequest request) {
        String requestHeaders = allowHeaders;

        if (!"*".equals(allowHeaders))
            return requestHeaders;

        /*
         * Access-Control-Allow-Credentials == true 인경우 wildcard(*) 가 안됨.
         * 그래서 Access-Control-Allow-Credentials == true &&  wildcard(*) 이면
         * Access-Control-Request-Headers 를 복사해서 넣음
         */
        requestHeaders = request.getHeader("Access-Control-Request-Headers");
        if (requestHeaders == null || requestHeaders.isEmpty())
            requestHeaders = allowHeaders;
        else
            log.trace("CORS preflight Access-Control-Request-Headers : {}", requestHeaders);

        return requestHeaders;
    }

}
