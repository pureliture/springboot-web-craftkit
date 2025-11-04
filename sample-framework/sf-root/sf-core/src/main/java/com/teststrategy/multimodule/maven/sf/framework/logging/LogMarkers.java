package com.teststrategy.multimodule.maven.sf.framework.logging;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class LogMarkers {


    private LogMarkers() {
        throw new IllegalStateException("LogMarker Constants class");
    }

    /**
     * Root LogMarker
     */
    public static final Marker ROOT = MarkerFactory.getMarker("ROOT");


    public static final Marker ENVIRONMENT = MarkerFactory.getMarker("ENVIRONMENT");

    /**
     * 데몬, 스케쥴러등 반복되는 작업의 모니터링용으로 사전 정의된 마커이다
     */
    public static final Marker REPEATABLE = MarkerFactory.getMarker("REPEATABLE");

    /**
     * 이벤트에 의해 비즈니스처리를 로깅하는 마커
     */
    public static final Marker EVENT = MarkerFactory.getMarker("EVENT");

    static {
        ENVIRONMENT.add(ROOT);
        REPEATABLE.add(ROOT);
        EVENT.add(ROOT);
    }


    public static Marker getNamed(String name) {
        Marker marker = MarkerFactory.getMarker(name);
        if (!marker.contains(ROOT)) {
            marker.add(ROOT);
        }
        return marker;
    }
}
