package com.springboot.craftkit.framework.util;

import com.springboot.craftkit.framework.application.ApplicationContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.UnaryOperator;


@Slf4j
public abstract class MessageUtil {

    private static final Map<String, UnaryOperator<Throwable>> otherCausedExtractorList = new ConcurrentHashMap<>();
    private static boolean useAbbreviatedName = false;
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    public static void registerOtherCausedExtractor(String name, UnaryOperator<Throwable> otherCausedExtractor) {
        if (name != null && otherCausedExtractor != null) {
            otherCausedExtractorList.put(name, otherCausedExtractor);
        }
    }

    /**
     * 약어 패키지명 사용 여부 초기화(애플리케이션 부팅 시 1회만 허용)
     */
    public static void initUseAbbreviatedName(boolean use) {
        if (initialized.compareAndSet(false, true)) {
            useAbbreviatedName = use;
        } else {
            // 재설정 시도를 차단(선호 정책에 따라 예외를 던지거나 로그만 남길 수 있음)
            log.debug("initUseAbbreviatedName() already initialized. Ignoring change.");
        }
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

    /**
     * Converts a Throwable's stack trace to a String.
     *
     * @param throwable the Throwable to get the stack trace from
     * @return the stack trace as a String
     */
    public static String getStackTraceString(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}