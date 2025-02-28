package com.teststrategy.multimodule.maven.sf.framework.scope;

import com.teststrategy.multimodule.maven.sf.framework.application.constant.CommonConstant;
import com.teststrategy.multimodule.maven.sf.framework.application.constant.HttpRequestConstant;

import java.util.List;
import java.util.Map;

public interface RequestScopeAttribute {

    String EMPTY = CommonConstant.INITIALIZE_VALUE_OF_EMPTY;
    String UNDEFINED_IP = HttpRequestConstant.UNDEFINED_IP;

    List<String> UNDEFINED_FIXED_FORWARDED_SERVICE = HttpRequestConstant.UNDEFINED_FIXED_FORWARDED_SERVICE;

    String GLOBAL_TRANSACTION_ID = HttpRequestConstant.HTTP_HEADER_TRANSACTION_ID;
    String USER_ID = HttpRequestConstant.HTTP_HEADER_USER_ID;
    String APPLICATION_NAME = HttpRequestConstant.HTTP_HEADER_APPLICATION_NAME;
    String PROGRAM_ID = HttpRequestConstant.HTTP_HEADER_PROGRAM_ID;
    String FORWARDED_SERVICE = HttpRequestConstant.HTTP_HEADER_FORWARDED_SERVICE;
    String LOG_LEVEL = "logLevel";

    // 인증인가서버에서 발행한 Access token의 사용자 정의 클레임
    String PRIVATE_CLAIMS = "Private-Claims";

    String CUSTOM_ATTRIBUTES = "Custom-Attributes";

    String KEY = "RequestScopeStore.key";

    String getUserId();

    void setUserId(String userId);

    String getApplicationName();

    void setApplicationName(String applicationName);

    String getProgramId();

    void setProgramId(String programId);

    String getClientIp();

    Map<String, Object> getPrivateClaims();

    Object getPrivateClaim(String key);

    Map<String, Object> getLocalAttributes();

    void setLocalAttribute(String key, Object value);

    Object getLocalAttribute(String key);

    Object getCustomAttribute(String key);

    String[] getCustomAttributeKeys();

    String getGtid();
}
