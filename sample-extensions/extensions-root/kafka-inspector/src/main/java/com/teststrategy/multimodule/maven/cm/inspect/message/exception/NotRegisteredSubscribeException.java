package com.teststrategy.multimodule.maven.cm.inspect.message.exception;


import java.io.Serial;

public class NotRegisteredSubscribeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -1728522934603271845L;

    public NotRegisteredSubscribeException(String message) {
        super(message);
    }
}
