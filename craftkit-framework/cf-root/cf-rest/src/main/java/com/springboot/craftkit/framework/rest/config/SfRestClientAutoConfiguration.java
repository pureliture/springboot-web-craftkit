package com.springboot.craftkit.framework.rest.config;

import com.springboot.craftkit.framework.rest.client.RestClientErrorHandler;
import com.springboot.craftkit.framework.rest.client.SfRestTemplate;
import com.springboot.craftkit.framework.rest.client.SfRestTemplateBuilder;
import com.springboot.craftkit.framework.rest.setting.HttpClientProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class SfRestClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SfRestTemplateBuilder sfRestTemplateBuilder(RestTemplateBuilder base) {
        return new SfRestTemplateBuilder(base);
    }

    @Bean
    @ConditionalOnMissingBean
    public SfRestTemplate sfRestTemplate(SfRestTemplateBuilder b, HttpClientProperties p, RestClientErrorHandler eh) {
        return b.delegate()
                .setConnectTimeout(p.getConnectTimeout())
                .setReadTimeout(p.getReadTimeout())
                .errorHandler(eh)
                .build(SfRestTemplate.class);
    }
}
