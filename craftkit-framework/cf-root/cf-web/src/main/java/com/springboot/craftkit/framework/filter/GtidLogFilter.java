package com.springboot.craftkit.framework.filter;

import com.springboot.craftkit.framework.application.constant.CommonConstant;
import com.springboot.craftkit.framework.application.constant.HeaderConstant;
import com.springboot.craftkit.framework.scope.RequestScopeUtil;
import com.springboot.craftkit.framework.scope.ScopeAttribute;
import com.springboot.craftkit.framework.util.HttpUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.boot.web.servlet.filter.OrderedFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 로그, 모니터링을 필터처리를 수행한다.
 */
@Setter
@Slf4j
public class GtidLogFilter implements OrderedFilter {
    private static final int GTID_MAX_BYTES_LENGTH = 50;

    /**
     *  Set the order for this filter.
     */
    int order = REQUEST_WRAPPER_FILTER_MAX_ORDER - 103;

    @Override
    public void init(FilterConfig filterConfig) {
    }

    /**
     * 이 메소드는 아래의 기능을 한다.
     * - Global Transaction ID(이른바, GTID)를 만든다.
     * - GTID 를 MDC 에 저장한다.
     * - GTID 를 HTTP Request scope attribute 에 저장한다.
     * - LogLevel 이 있으면 Request scope attribute 에 저장한다.
     *
     * @param request servlet request
     * @param response servlet response
     * @param chain filter chain
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // gtid 를 설정한다.
        setGtid(request, response);

        // 로그레벨을 설정한다.
        setLogLevel(request);

        chain.doFilter(request, response);

        MDC.clear();
    }

    /**
     * Global-Transaction-ID 추출 및 세션 저장
     *
     * @param request  http servlet request
     * @param response http response
     */
    private void setGtid(ServletRequest request, ServletResponse response) {
        // Global-Transaction-ID 를 가져온다.
        String gtid = ((HttpServletRequest) request).getHeader(HeaderConstant.HEADER_GLOBAL_TRANSACTION_ID);

        // Global-Transaction-ID 없으면 생성한다. 
        if (StringUtils.isBlank(gtid)) {
            gtid = HttpUtil.generateNewGtid();
        } else if (!isValidGtid(gtid)) { //gtid 가 50byte 가 넘으면 새로 생성
            String newGtid = "N-" + HttpUtil.generateNewGtid();
            log.error("gtid changed(gtid bytes length is larger then {}) - origin gtid : {} , new gtid : {} ", GTID_MAX_BYTES_LENGTH, gtid, newGtid);
            gtid = newGtid;
        }

        // response 에 기록한다.
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader(HeaderConstant.HEADER_GLOBAL_TRANSACTION_ID, gtid);

        // 로그에 기록 한다.
        MDC.put(CommonConstant.LOG_MDC_GTID, gtid);

        // 세션에 기록해 둔다.
        ((ScopeAttribute) RequestScopeUtil.getAttribute()).setGtid(gtid);
    }

    private static boolean isValidGtid(String str) {
        try {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            return bytes.length <= GTID_MAX_BYTES_LENGTH;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * LogLevel 추출 및 세션 저장
     *
     * @param request http servlet request
     */
    private void setLogLevel(ServletRequest request) {
        // log level 을 추출한다.
        String levelValue = ((HttpServletRequest) request).getHeader(HeaderConstant.HEADER_LOGLEVEL_KEY);

        // 세션에 기록한다.
        ((ScopeAttribute) RequestScopeUtil.getAttribute()).setLogLevel(levelValue);
    }

    @Override
    public void destroy() {
        MDC.clear();
    }

    @Override
    public int getOrder() {
        return order;
    }

}
