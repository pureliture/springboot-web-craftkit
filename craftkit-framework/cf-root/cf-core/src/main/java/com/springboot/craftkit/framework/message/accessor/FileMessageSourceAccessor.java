package com.springboot.craftkit.framework.message.accessor;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;

/**
 * 파일 기반 구현: org.springframework.context.support.MessageSourceAccessor 위임 어댑터
 */
public class FileMessageSourceAccessor implements MessageSourceAccessor {

    private final MessageSource messageSource;
    private final org.springframework.context.support.MessageSourceAccessor delegate;

    public FileMessageSourceAccessor(ReloadableResourceBundleMessageSource messageSource) {
        this.messageSource = messageSource;
        this.delegate = new org.springframework.context.support.MessageSourceAccessor(messageSource);
    }

    @Override
    public org.springframework.context.MessageSource getMessageSource() {
        return messageSource;
    }

    @Override
    public String getMessage(String code, String defaultMessage) {
        return delegate.getMessage(code, defaultMessage);
    }

    @Override
    public String getMessage(String code, String defaultMessage, Locale locale) {
        return delegate.getMessage(code, defaultMessage, locale);
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage) {
        return delegate.getMessage(code, args, defaultMessage);
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        return delegate.getMessage(code, args, defaultMessage, locale);
    }

    @Override
    public String getMessage(String code) {
        return delegate.getMessage(code);
    }

    @Override
    public String getMessage(String code, Locale locale) {
        return delegate.getMessage(code, locale);
    }

    @Override
    public String getMessage(String code, Object[] args) {
        return delegate.getMessage(code, args);
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) {
        return delegate.getMessage(code, args, locale);
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable) {
        return delegate.getMessage(resolvable);
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) {
        return delegate.getMessage(resolvable, locale);
    }
}
