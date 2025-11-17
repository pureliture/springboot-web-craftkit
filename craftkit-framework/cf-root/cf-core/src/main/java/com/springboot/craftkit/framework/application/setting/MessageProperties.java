package com.springboot.craftkit.framework.application.setting;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;

@Slf4j
@Data
@ConfigurationProperties(prefix = MessageProperties.PREFIX, ignoreUnknownFields = false)
public class MessageProperties {

    public static final String PREFIX = "sf.messages";

    private String type;
    private int cacheSeconds = -1;
    private String basename;
    private String[] basenames = new String[] {"classpath:/messages/message"};
    private String encoding = "UTF-8";
    private String tableName;
    private String loadType;
    private String messageColumn;
    private String codeColumn;
    private String localeColumn;
    private String messageDefaultQuery;
    private String whereClause;
    private String refreshCron;
    private boolean enabledRefreshAll = false;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MessageProperties [");
        if (type != null)
            builder.append("type=").append(type).append(", ");
        builder.append("cacheSeconds=").append(cacheSeconds).append(", ");
        if (basename != null)
            builder.append("basename=").append(basename).append(", ");
        if (basenames != null)
            builder.append("basenames=").append(Arrays.toString(basenames)).append(", ");
        if (encoding != null)
            builder.append("encoding=").append(encoding).append(", ");
        if (tableName != null)
            builder.append("tableName=").append(tableName).append(", ");
        if (loadType != null)
            builder.append("loadType=").append(loadType).append(", ");
        if (messageColumn != null)
            builder.append("messageColumn=").append(messageColumn).append(", ");
        if (codeColumn != null)
            builder.append("codeColumn=").append(codeColumn).append(", ");
        if (localeColumn != null)
            builder.append("localeColumn=").append(localeColumn).append(", ");
        if (messageDefaultQuery != null)
            builder.append("messageDefaultQuery=").append(messageDefaultQuery).append(", ");
        if (whereClause != null)
            builder.append("whereClause=").append(whereClause).append(", ");
        if (refreshCron != null)
            builder.append("refreshCron=").append(refreshCron).append(", ");
        builder.append("enabledRefreshAll=").append(enabledRefreshAll).append("]");
        return builder.toString();
    }
}