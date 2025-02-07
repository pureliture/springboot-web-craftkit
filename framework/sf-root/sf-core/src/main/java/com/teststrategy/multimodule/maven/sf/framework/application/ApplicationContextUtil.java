package com.teststrategy.multimodule.maven.sf.framework.application;

import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;


public class ApplicationContextUtil {

    @Getter
    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext context) throws BeansException {
        if (applicationContext == null) {
            applicationContext = context;
        }
    }

    public static Object getBean(Class<?> clazz) {
        return applicationContext.getBean(clazz);
    }

    public static Object getBean(String clazz) {
        return applicationContext.getBean(clazz);
    }
}