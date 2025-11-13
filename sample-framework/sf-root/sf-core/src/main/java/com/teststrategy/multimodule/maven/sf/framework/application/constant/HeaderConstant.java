package com.teststrategy.multimodule.maven.sf.framework.application.constant;

import java.util.List;

/**
 * Channel-agnostic header constants shared across transports (HTTP, Kafka, etc.).
 * Use these for headers that are always present regardless of channel.
 */
public final class HeaderConstant {

    private HeaderConstant() {
        throw new IllegalStateException("Utility class");
    }

    // Common custom header keys
    public static final String HEADER_USER_ID = "USER-ID";
    public static final String HEADER_PROGRAM_ID = "PROGRAM-ID";
    public static final String HEADER_FORWARDED_SERVICE = "Forwarded-Service";

    public static final String HEADER_GLOBAL_TRANSACTION_ID = "Global-Transaction-ID";
    public static final String HEADER_LOGLEVEL_KEY = "LogLevel";

    // Gateway signature header (transport-agnostic)
    public static final String HEADER_APPLICATION_NAME = "APPLICATION-NAME";
    public static final String HEADER_SIGNATURE = "Header-Authorization";

    // Initialization/common values related to header composition
    public static final String HEADER_DATE_TIMEFORMAT = "yyyyMMdd'T'HHmmssZ";

    public static final String UNDEFINED_SERVICE = "undefined";
    // fixed list :: could not add elements
    public static final List<String> UNDEFINED_FIXED_FORWARDED_SERVICE = List.of(UNDEFINED_SERVICE);
}
