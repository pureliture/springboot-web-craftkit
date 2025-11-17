package com.springboot.craftkit.framework.message.accessor;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.lang.Nullable;

import java.util.Locale;

public interface MessageSourceAccessor {

    MessageSource getMessageSource() ;

    /**
     * Retrieve the message for the given code and the default Locale.
     * @param code code of the message
     * @param defaultMessage the String to return if the lookup fails
     * @return the message
     */
    String getMessage(String code, String defaultMessage);

    /**
     * Retrieve the message for the given code and the given Locale.
     * @param code code of the message
     * @param defaultMessage the String to return if the lookup fails
     * @param locale the Locale in which to do lookup
     * @return the message
     */
    String getMessage(String code, String defaultMessage, Locale locale);

    /**
     * Retrieve the message for the given code and the default Locale.
     * @param code code of the message
     * @param args arguments for the message, or {@code null} if none
     * @param defaultMessage the String to return if the lookup fails
     * @return the message
     */
    String getMessage(String code, @Nullable Object[] args, String defaultMessage);

    /**
     * Retrieve the message for the given code and the given Locale.
     * @param code code of the message
     * @param args arguments for the message, or {@code null} if none
     * @param defaultMessage the String to return if the lookup fails
     * @param locale the Locale in which to do lookup
     * @return the message
     */
    String getMessage(String code, @Nullable Object[] args, String defaultMessage, Locale locale);

    /**
     * Retrieve the message for the given code and the default Locale.
     * @param code code of the message
     * @return the message
     * @throws org.springframework.context.NoSuchMessageException if not found
     */
    String getMessage(String code) throws NoSuchMessageException;

    /**
     * Retrieve the message for the given code and the given Locale.
     * @param code code of the message
     * @param locale the Locale in which to do lookup
     * @return the message
     * @throws org.springframework.context.NoSuchMessageException if not found
     */
    String getMessage(String code, Locale locale) throws NoSuchMessageException;

    /**
     * Retrieve the message for the given code and the default Locale.
     * @param code code of the message
     * @param args arguments for the message, or {@code null} if none
     * @return the message
     * @throws org.springframework.context.NoSuchMessageException if not found
     */
    String getMessage(String code, @Nullable Object[] args) throws NoSuchMessageException;

    /**
     * Retrieve the message for the given code and the given Locale.
     * @param code code of the message
     * @param args arguments for the message, or {@code null} if none
     * @param locale the Locale in which to do lookup
     * @return the message
     * @throws org.springframework.context.NoSuchMessageException if not found
     */
    String getMessage(String code, @Nullable Object[] args, Locale locale) throws NoSuchMessageException;

    /**
     * Retrieve the given MessageSourceResolvable (e.g. an ObjectError instance)
     * in the default Locale.
     * @param resolvable the MessageSourceResolvable
     * @return the message
     * @throws org.springframework.context.NoSuchMessageException if not found
     */
    String getMessage(MessageSourceResolvable resolvable) throws NoSuchMessageException;

    /**
     * Retrieve the given MessageSourceResolvable (e.g. an ObjectError instance)
     * in the given Locale.
     * @param resolvable the MessageSourceResolvable
     * @param locale the Locale in which to do lookup
     * @return the message
     * @throws org.springframework.context.NoSuchMessageException if not found
     */
    String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException;

}
