package com.teststrategy.multimodule.maven.cm.inspect.message.service;

import com.teststrategy.multimodule.maven.cm.inspect.message.dto.AllowedSubscribeMessage;

import java.util.List;

public interface SubscribeInspector {

    default boolean isAllowed(String consumerGroup, String topic) {
        return true;
    }

    default boolean isNotAllowed(String consumerGroup, String topic) {
        return !isAllowed(consumerGroup, topic);
    }

    void updateMshMap(List<AllowedSubscribeMessage> subscribeMessageList);

    /**
     * 기동시점 "비동기메시징포털" 연결이 실패하면, 일시정지를 설정한다.
     * 만약, 스케쥴에 의한 갱신시점 연결실패이면, 기존 데이터를 유지하고, 일시정지 하지 않는다.
     */
    void setSuspended();
}
