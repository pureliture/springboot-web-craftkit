package com.teststrategy.multimodule.maven.sf.framework.application.setting;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Logging properties for sample-framework.
 * Keys:
 * - sample.log.auto-logging-enabled
 * - sample.log.include-arguments
 * - sample.log.include-returns
 * - sample.log.use-abbreviated-package-name
 */
@Data
@ToString
@NoArgsConstructor
@ConfigurationProperties(prefix = "sample.log", ignoreUnknownFields = true)
public class LoggingProperties {

    private boolean autoLoggingEnabled = false;
    private boolean includeArguments = false;
    private boolean includeReturns = false;
    private boolean useAbbreviatedPackageName = false;
}
