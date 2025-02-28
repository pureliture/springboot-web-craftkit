package com.teststrategy.multimodule.maven.sf.framework.application.constant;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class CommonConstant {

    private CommonConstant() { super(); }


    public static final Charset DEFAULT_CHARSETS = StandardCharsets.UTF_8;
    public static final String DEFAULT_ENCODING = DEFAULT_CHARSETS.toString();

    public static final String INITIALIZE_VALUE_OF_EMPTY = "empty";
    @Deprecated
    public static final String FORWARDED_ATTRIBUTE = "Forwarded-Attribute";
}
