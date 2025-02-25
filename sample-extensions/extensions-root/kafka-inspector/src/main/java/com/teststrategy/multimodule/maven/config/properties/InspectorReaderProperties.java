package com.teststrategy.multimodule.maven.config.properties;

import com.teststrategy.multimodule.maven.sf.framework.logging.LogMarkers;
import com.teststrategy.multimodule.maven.sf.framework.util.PropertyUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@Setter
@Getter
public abstract class InspectorReaderProperties {

    protected boolean enabled = true;
    protected String resourceUrl;

    public String getParsedResourceUrl(String applicationName) {
        return this.resourceUrl.replace("{application-name}", applicationName)
                .replace("{profile}", PropertyUtil.getLastActiveProfile());
    }

    public boolean isValidUrlAndParams(String applicationName) {
        if (this.resourceUrl == null) {
            return false;
        } else if (resourceUrl.contains("{application-name}}") && StringUtils.isBlank(applicationName)) {
            return false;
        }
        return true;
    }

    public boolean isDisabled() {
        return !enabled;
    }

    protected void toLog(Logger log, String prefix) {
        log.info(LogMarkers.ENVIRONMENT, "{} enabled={}", prefix, this.enabled);
        log.info(LogMarkers.ENVIRONMENT, "{} resource-url={}", prefix, this.resourceUrl);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(" [enabled=").append(enabled)
                .append(", resourceUrl=").append(resourceUrl)
                .append("]");
        return builder.toString();
    }
}
