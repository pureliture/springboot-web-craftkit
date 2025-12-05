package com.springboot.craftkit.framework.application.constant;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Common constants used across the framework.
 */
public class CommonConstants {

    private CommonConstants() {
        super();
    }

    /**
     * Default policy name
     */
    public static final String POLICY_DEFAULT = "default";
    public static final String POLICY = "_policy";

    /**
     * Default charset and encoding
     */
    public static final Charset DEFAULT_CHARSETS = StandardCharsets.UTF_8;
    public static final String DEFAULT_ENCODING = DEFAULT_CHARSETS.toString();
    public static final String UNDEFINED = "undefined";

    /**
     * Paging constants
     */
    public static final String PAGING_ROW_SIZE = "rowSize";
    public static final String PAGING_PAGE_SIZE = "pageSize";
    public static final String PAGING_PAGE_NO = "pageNo";
    public static final String PAGING_START = "start";
    public static final String PAGING_LAST = "last";
    public static final String PAGING_TOTAL_RECORD = "totalRecord";
    public static final String PAGING_TOTAL_PAGE = "totalPage";
    public static final String PAGING = "pageNo";
    public static final Object PAGING_POLICY = "paging_policy";
    public static final String PAGING_COUNTABLE = "countable";
    public static final String IF_ORACLE_USE_OFFSET = "if-oracle-use-offset";

    public static final int INITIAL_ROW_SIZE = 20;
    public static final int INITIAL_PAGE_SIZE = 5;

    /**
     * Server result constants
     */
    public static final String SERVER_MESSAGE = "SERVER_MESSAGE";
    public static final String SERVER_RESULT = "SERVER_RESULT";
    public static final String SERVER_PAGING = "SERVER_PAGING";

    /**
     * File upload policy constants
     */
    public static final String UPLOAD_FILE_PATH = "path";
    public static final String UPLOAD_FILE_EXT = "ext";
    public static final String UPLOAD_FILE_MAX_SIZE = "size";
    public static final String UPLOAD_PATTERN = "pattern";
    public static final String UPLOAD_PATTERN_SIMPLE = "simple";
    public static final String UPLOAD_PATTERN_DATE = "date";
    public static final String UPLOAD_PATTERN_ALPHANUMERIC = "alphanumeric";

    /**
     * Result codes
     */
    public static final String SUCCESS_CODE = "200";
    public static final String SUCCESS_MESSAGE = "Successfully processed.";
    public static final String NO_CONTENT_CODE = "204";
    public static final String NO_CONTENT_MESSAGE = "No results found.";

    public static final String FAILURE_CODE = "500";
    public static final String FAILURE_MESSAGE = "An error occurred during processing. Please contact the administrator.";

    /**
     * Session information
     */
    public static final String HTTP_SESSION_KEY = "SESSION_KEY";

    /**
     * Exception key
     */
    public static final String REQUEST_EXCEPTION_KEY = "REQUEST_EXCEPTION_KEY";

    /**
     * Mail template constants
     */
    public static final String PLACEHOLDER = "placeholder";
    public static final String TEMPLATE = "template";

    /**
     * REST service provider constants
     */
    public static final String REST_SERVICE_MESSAGE = "provider_server_message";
    public static final String REST_SERVICE = "rest_service";
    public static final String APIM_TOKEN_CACHE = "apimTokenCache";
    public static final String COMMAND = "c";
    public static final String MENU_CACHE = "menuCache";
    public static final String PATH_MAP = "pathMap";
    public static final String PATH_MAP_ITEM = "pathMap[%d]";

    public static final String LOG_MDC_GTID = "gtid";

    /**
     * Constant values for initialization
     */
    public static final String INITIALIZE_VALUE_OF_EMPTY = "empty";

    @Deprecated
    public static final String FORWARDED_ATTRIBUTE = "X-Forwarded-Attribute";

    /**
     * User type code constant
     */
    public static final String USER_TYPE_CODE = "usrTypCd";

    /**
     * Development profiles
     */
    public static final String[] DEVELOP_PROFILES = {"dev", "local", "test", "edu"};
}
