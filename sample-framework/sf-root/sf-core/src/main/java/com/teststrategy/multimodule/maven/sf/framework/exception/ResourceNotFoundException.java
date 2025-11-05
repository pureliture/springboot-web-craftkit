package com.teststrategy.multimodule.maven.sf.framework.exception;

/**
 * 404 Not Found 수준의 리소스 미존재에 대한 카테고리 예외입니다.
 */
public class ResourceNotFoundException extends CraftkitSystemException {

    public ResourceNotFoundException() {
        super(CommonErrorCode.RESOURCE_NOT_FOUND);
    }

    public ResourceNotFoundException(Object args) {
        super(CommonErrorCode.RESOURCE_NOT_FOUND, args);
    }

    public ResourceNotFoundException(Throwable cause) {
        super(CommonErrorCode.RESOURCE_NOT_FOUND, cause);
    }

    public ResourceNotFoundException(Object args, Throwable cause) {
        super(CommonErrorCode.RESOURCE_NOT_FOUND, args, cause);
    }
}
