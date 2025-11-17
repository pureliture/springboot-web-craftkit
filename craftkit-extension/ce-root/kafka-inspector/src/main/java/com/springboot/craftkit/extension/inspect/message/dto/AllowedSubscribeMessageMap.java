package com.springboot.craftkit.extension.inspect.message.dto;

import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.util.concurrent.ConcurrentHashMap;

public class AllowedSubscribeMessageMap extends ConcurrentHashMap<String, AllowedSubscribeMessage> {

    @Serial
    private static final long serialVersionUID = 1748762363397750447L;

    private final String delimeter = "::";

    public void put(String cgId, String topic, AllowedSubscribeMessage value) {
        String key = StringUtils.join(cgId, delimeter, topic);
        this.put(key, value);
    }

    public boolean containsKey(String cgId, String topic) {
        String key = StringUtils.join(cgId, delimeter, topic);
        return this.containsKey(key);
    }

    public AllowedSubscribeMessage get(String cgId, String topic) {
        String key = StringUtils.join(cgId, delimeter, topic);
        return this.get(key);
    }
}
