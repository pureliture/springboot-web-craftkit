package com.teststrategy.multimodule.maven.sf.framework.util;

import com.teststrategy.multimodule.maven.sf.framework.application.constant.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Slf4j
public class StringUtil {

    private StringUtil() { }
    
    public static String encodeURIComponent(String content) {
        if (StringUtils.isBlank(content))
            return "";

        try {
            return URLEncoder.encode(content, CommonConstant.DEFAULT_ENCODING).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            log.error("StringUtil.decodeURIComponent error {} .. ignored : {}", e.getMessage(), content);
            return content;
        }
    }
}
