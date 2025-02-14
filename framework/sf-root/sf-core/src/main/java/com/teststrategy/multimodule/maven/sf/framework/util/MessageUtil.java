package com.teststrategy.multimodule.maven.sf.framework.util;

import com.teststrategy.multimodule.maven.sf.framework.application.ApplicationContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;


@Slf4j
public abstract class MessageUtil {

    private static Map<String, UnaryOperator<Throwable>> otherCausedExtractorList = new ConcurrentHashMap<>();
    private static boolean useAbbreviatedName = false;

    protected MessageUtil(String name, UnaryOperator<Throwable> otherCausedExtractor) {
        if(otherCausedExtractor != null) {
            otherCausedExtractorList.put(name,otherCausedExtractor);
        }
    }

    protected MessageUtil(String name, UnaryOperator<Throwable> otherCausedExtractor, boolean useAbbreviatedName) {
        if(otherCausedExtractor != null) {
            otherCausedExtractorList.put(name,otherCausedExtractor);
        }
        MessageUtil.useAbbreviatedName = useAbbreviatedName;
    }

    protected MessageUtil(boolean useAbbreviatedName) {
        MessageUtil.useAbbreviatedName = useAbbreviatedName;
    }


    public static MessageSourceAccessor getMessageSourceAccessor() {
        try {
            return (MessageSourceAccessor) ApplicationContextUtil.getBean("messageSourceAccessor");
        } catch (Exception e) {
            log.trace("getMessageSourceAccessor() error is skipped : {}", e.getMessage());
            return null;
        }
    }

    public static String getMessage(String code) {
        return getDefaultMessageIfNone(code, LocaleContextHolder.getLocale(), code);
    }

    public static String getMessage(String code, Object... args) {
        return getDefaultMessageIfNone(code, LocaleContextHolder.getLocale(), code, args);
    }

    public static String getMessage(Locale locale, String code, Object... args) {
        return getDefaultMessageIfNone(code, locale, code, args);
    }

    public static String getDefaultMessageIfNone(String defaultMessage, String code, Object... args) {
        return getDefaultMessageIfNone(defaultMessage, LocaleContextHolder.getLocale(), code, args);
    }

    @SuppressWarnings("java:S2259")
    public static String getDefaultMessageIfNone(String defaultMessage, Locale locale, String code, Object... args) {
        String message;
        try {
            message = getMessageSourceAccessor().getMessage(code, args, defaultMessage, locale);
        } catch (Exception e) {
            log.trace("getMessage() error is skipped : {}", e.getMessage());
            message = code;
        }

        return message;
    }

    /**
     * Retrieve the given MessageSourceResolvable (e.g. an ObjectError instance)
     * in the default Locale.
     * @param resolvable the MessageSourceResolvable
     * @return the message
     * @throws org.springframework.context.NoSuchMessageException if not found
     */
    @SuppressWarnings("java:S2259")
    public static String getMessage(MessageSourceResolvable resolvable) throws NoSuchMessageException {
        String message;
        try {
            message = getMessageSourceAccessor().getMessage(resolvable);
        } catch (Exception e) {
            log.trace("getString() error is skipped : {}", e.getMessage());
            message = resolvable.getDefaultMessage();
        }

        return message;
    }

    /**
     * Retrieve the given MessageSourceResolvable (e.g. an ObjectError instance)
     * in the given Locale.
     * @param resolvable the MessageSourceResolvable
     * @param locale the Locale in which to do lookup
     * @return the message
     * @throws org.springframework.context.NoSuchMessageException if not found
     */
    @SuppressWarnings("java:S2259")
    public static String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        String message;
        try {
            message = getMessageSourceAccessor().getMessage(resolvable, locale);
        } catch (Exception e) {
            log.trace("getMessage() error is skipped : {}", e.getMessage());
            message = resolvable.getDefaultMessage();
        }

        return message;
    }
}
