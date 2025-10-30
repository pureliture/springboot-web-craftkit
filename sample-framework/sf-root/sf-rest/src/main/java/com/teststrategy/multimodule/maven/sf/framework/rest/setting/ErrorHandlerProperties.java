package com.teststrategy.multimodule.maven.sf.framework.rest.setting;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Properties to enable and tune advanced business error detection on 2xx responses.
 *
 * Prefix: sample-framework.rest.error-handler
 */
@ConfigurationProperties(prefix = ErrorHandlerProperties.PREFIX)
public class ErrorHandlerProperties {

    public static final String PREFIX = "sample-framework.rest.error-handler";

    /** Enable advanced 2xx JSON body inspection. */
    private boolean enabled = false;

    /** Dot-path to the business result code in JSON body. e.g. "code" or "data.code" */
    private String jsonPathCode = "code";

    /** Dot-path to the business message in JSON body. e.g. "message" or "error.message" */
    private String jsonPathMessage = "message";

    /** Success code white-list. If present and the code is NOT one of them, it is treated as error. */
    private Set<String> successCodes = new LinkedHashSet<>(Arrays.asList("OK", "SUCCESS", "0000", "0"));

    /** Content types to inspect. JSON types by default. */
    private Set<String> inspectContentTypes = new LinkedHashSet<>(Arrays.asList("application/json", "application/*+json"));

    /** Only inspect when HTTP status is 2xx. */
    private boolean onlyOn2xx = true;

    /** Treat empty or blank body as success (no inspection). */
    private boolean emptyBodyIsSuccess = true;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getJsonPathCode() { return jsonPathCode; }
    public void setJsonPathCode(String jsonPathCode) { this.jsonPathCode = jsonPathCode; }

    public String getJsonPathMessage() { return jsonPathMessage; }
    public void setJsonPathMessage(String jsonPathMessage) { this.jsonPathMessage = jsonPathMessage; }

    public Set<String> getSuccessCodes() { return successCodes; }
    public void setSuccessCodes(Set<String> successCodes) { this.successCodes = normalize(successCodes); }

    public Set<String> getInspectContentTypes() { return inspectContentTypes; }
    public void setInspectContentTypes(Set<String> inspectContentTypes) { this.inspectContentTypes = normalize(inspectContentTypes); }

    public boolean isOnlyOn2xx() { return onlyOn2xx; }
    public void setOnlyOn2xx(boolean onlyOn2xx) { this.onlyOn2xx = onlyOn2xx; }

    public boolean isEmptyBodyIsSuccess() { return emptyBodyIsSuccess; }
    public void setEmptyBodyIsSuccess(boolean emptyBodyIsSuccess) { this.emptyBodyIsSuccess = emptyBodyIsSuccess; }

    private static Set<String> normalize(Set<String> input) {
        if (input == null) return null;
        return input.stream().filter(s -> s != null).map(String::trim).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // Helper for tests to set via comma-separated list
    public void setSuccessCodesCsv(String csv) {
        if (csv == null) return;
        List<String> list = Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        this.successCodes = new LinkedHashSet<>(list);
    }
}
