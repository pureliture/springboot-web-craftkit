package com.teststrategy.multimodule.maven.sf.framework.application.shutdown;

public interface ShutdownSupport {

    default void addShutdownHelper(ShutdownHelper helper) {};
}
