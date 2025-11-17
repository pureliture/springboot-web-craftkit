package com.springboot.craftkit.framework.application.constant;

import java.util.List;

/**
 * 전송 채널(HTTP, Kafka 등)과 무관하게 공통으로 사용하는 헤더 상수 모음입니다.
 * 어떤 채널에서도 항상 존재하는(또는 사용할 수 있는) 헤더에 대해 이 상수를 사용하세요.
 */
public final class HeaderConstant {

    private HeaderConstant() {
        throw new IllegalStateException("Utility class");
    }

    // 공통 커스텀 헤더 키
    public static final String HEADER_USER_ID = "USER-ID";
    public static final String HEADER_PROGRAM_ID = "PROGRAM-ID";
    public static final String HEADER_FORWARDED_SERVICE = "Forwarded-Service";

    public static final String HEADER_GLOBAL_TRANSACTION_ID = "Global-Transaction-ID";
    public static final String HEADER_LOGLEVEL_KEY = "LogLevel";

    // 게이트웨이 서명 관련 헤더(전송 채널과 무관)
    public static final String HEADER_APPLICATION_NAME = "APPLICATION-NAME";
    public static final String HEADER_SIGNATURE = "Header-Authorization";

    // 헤더 구성과 관련된 초기/공통 값
    public static final String HEADER_DATE_TIMEFORMAT = "yyyyMMdd'T'HHmmssZ";

    public static final String UNDEFINED_SERVICE = "undefined";
    // 고정 리스트 :: 요소 추가 불가
    public static final List<String> UNDEFINED_FIXED_FORWARDED_SERVICE = List.of(UNDEFINED_SERVICE);
}