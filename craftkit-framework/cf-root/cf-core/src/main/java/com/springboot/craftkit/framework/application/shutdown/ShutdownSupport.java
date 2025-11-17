package com.springboot.craftkit.framework.application.shutdown;

public interface ShutdownSupport {

    default void addShutdownHelper(ShutdownHelper helper) {};
}
