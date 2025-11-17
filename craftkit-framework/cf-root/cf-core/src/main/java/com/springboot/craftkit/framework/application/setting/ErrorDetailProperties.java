package com.springboot.craftkit.framework.application.setting;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 에러 상세 노출 결정 관련 설정.
 * 접두사: sample.error
 */
@Data
@ToString
@NoArgsConstructor
@ConfigurationProperties(prefix = "sample.error", ignoreUnknownFields = true)
public class ErrorDetailProperties {

    /** 결정 필터를 등록하고 활성화할지 여부. */
    private boolean filterEnabled = true;

    /** 에러 상세 정보를 볼 수 있도록 신뢰된 서비스 목록. */
    private List<String> trustedServices = new ArrayList<>();

    /** 헤더가 없거나 신뢰되지 않는 경우의 기본 동작. */
    private boolean defaultEnabled = false;
}
