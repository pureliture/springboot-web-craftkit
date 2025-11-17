package com.springboot.craftkit.framework.application.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Opt-in annotation to include method/class in auto logging scope.
 */
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface AutoLogging {
}
