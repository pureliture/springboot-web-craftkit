package com.springboot.craftkit.framework.rest.client;

import com.springboot.craftkit.framework.rest.client.chain.UriTemplateHandlerInterceptorChain;
import org.springframework.beans.BeanInfoFactory;
import org.springframework.beans.ExtendedBeanInfoFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Chain element that expands URI template variables from a DTO object via JavaBeans getters
 * when expand(String, Object...) is called with a single non-Map argument.
 * Falls back to delegation for all other cases.
 */
public class DtoUriTemplateHandler extends UriTemplateHandlerInterceptorChain {

    private static final BeanInfoFactory BEAN_INFO_FACTORY = new ExtendedBeanInfoFactory();

    @Override
    public int getOrder() {
        return 10; // run after DomainUriTemplateHandler
    }

    @NonNull
    @Override
    public URI expand(@NonNull String uriTemplate, @NonNull Map<String, ?> uriVariables) {
        return next.expand(uriTemplate, uriVariables);
    }

    @NonNull
    @Override
    public URI expand(@NonNull String uriTemplate, @NonNull Object... uriVariables) {
        if (uriVariables != null && uriVariables.length == 1 && !(uriVariables[0] instanceof Map)) {
            Object dto = uriVariables[0];
            if (dto != null && !ClassUtils.isPrimitiveOrWrapper(dto.getClass()) && !(dto instanceof String)) {
                Map<String, Object> map = toPropertyMap(dto);
                return next.expand(uriTemplate, map);
            }
        }
        return next.expand(uriTemplate, uriVariables);
    }

    private Map<String, Object> toPropertyMap(Object bean) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            BeanInfo info = (BEAN_INFO_FACTORY != null)
                    ? BEAN_INFO_FACTORY.getBeanInfo(bean.getClass())
                    : Introspector.getBeanInfo(bean.getClass());
            if (info == null) {
                info = Introspector.getBeanInfo(bean.getClass());
            }
            for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                Method readMethod = pd.getReadMethod();
                if (readMethod != null && !"class".equals(pd.getName())) {
                    Object value = readMethod.invoke(bean);
                    if (value != null) {
                        map.put(pd.getName(), value);
                    }
                }
            }
        } catch (Exception ignore) {
            // fallback silently; partial map is okay
        }
        return map;
    }
}
