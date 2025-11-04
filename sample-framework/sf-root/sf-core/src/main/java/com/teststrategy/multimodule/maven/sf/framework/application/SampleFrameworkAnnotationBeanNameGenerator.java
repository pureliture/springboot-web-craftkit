package com.teststrategy.multimodule.maven.sf.framework.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.util.Assert;


public class SampleFrameworkAnnotationBeanNameGenerator extends AnnotationBeanNameGenerator {

    static Logger log = LoggerFactory.getLogger(SampleFrameworkAnnotationBeanNameGenerator.class);

    @Override
    public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {

        String beanClassName = definition.getBeanClassName();
        String beanName;

        // configuration Bean :: org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition
        // componentscan Bean :: org.springframework.context.annotation.ScannedGenericBeanDefinition
        // 두가지를 구분하고자 하며, 기본은 AnnotationBeanNameGenerator 의 BeanName 생성 로직을 적용
        if (definition instanceof AnnotatedGenericBeanDefinition) {
            beanName = beanClassName;
        } else {
            beanName = super.generateBeanName(definition, registry);
        }

        if (registry.isBeanNameInUse(beanName)) {
            Assert.state(beanClassName != null, "No bean class name set");
            log.warn("Class {} 의 BeanName 이 중복되어 이름을 {} 로 변경합니다.", beanClassName, beanClassName);
            return beanClassName;
        }

        log.trace("Class {} 의 BeanName {} 이 생성 되었습니다.", beanClassName, beanName);
        return beanName;
    }
}
