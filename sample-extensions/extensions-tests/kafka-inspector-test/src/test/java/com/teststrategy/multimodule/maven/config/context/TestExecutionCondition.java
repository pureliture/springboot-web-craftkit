package com.teststrategy.multimodule.maven.config.context;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.List;


public class TestExecutionCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {

        List<String> allowedProfileList = List.of("default", "test");

        String activeProfile = System.getProperty("spring.profiles.active", "default");

        if (allowedProfileList.contains(activeProfile)) {
            return ConditionEvaluationResult.enabled("Run tests by active profile");
        }
        return ConditionEvaluationResult.disabled("Skip tests by active profile");
    }
}
