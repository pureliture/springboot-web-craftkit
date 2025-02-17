package com.teststrategy.multimodule.maven.config;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;

import java.util.Arrays;
import java.util.List;

public class AutoConfigurationImportFilterForSpringBootTest implements AutoConfigurationImportFilter {

    private static final List<String> EXCLUDED_PACKAGE = Arrays.asList("io.eventuate");


    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
        boolean[] matchResults = new boolean[autoConfigurationClasses.length];


        for (int i = 0; i < autoConfigurationClasses.length; i++) {
            String className = autoConfigurationClasses[i];
            // 특정 패키지 경로에 해당하는 클래스 제외
            if (className == null) {
                matchResults[i] = true; // 포함
            } else if (EXCLUDED_PACKAGE.stream().anyMatch(className::startsWith)) {
                matchResults[i] = false; // 제외
            } else {
                matchResults[i] = true; // 포함
            }
        }
        return matchResults;
    }

}
