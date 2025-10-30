package com.teststrategy.multimodule.maven.sf.framework.rest;

import com.teststrategy.multimodule.maven.sf.framework.application.setting.DomainApiProperties;
import com.teststrategy.multimodule.maven.sf.framework.application.setting.DomainProperties;
import com.teststrategy.multimodule.maven.sf.framework.rest.config.DomainSupportAutoConfiguration;
import com.teststrategy.multimodule.maven.sf.framework.rest.config.RestAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class DomainSupportAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RestAutoConfiguration.class, DomainSupportAutoConfiguration.class));

    @Test
    void domainBeansLoaded_whenPropertiesConfigured() {
        contextRunner
                .withPropertyValues(
                        "sample-framework.rest.domain.config=classpath:config/domain.yml",
                        "sample-framework.rest.domain.api.config=classpath:config/domain-api.yml"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(DomainProperties.class);
                    assertThat(context).hasSingleBean(DomainApiProperties.class);

                    DomainProperties domains = context.getBean(DomainProperties.class);
                    assertThat(domains.hasDomain("demo")).isTrue();
                    assertThat(domains.getDomainUrl("demo")).isEqualTo("http://localhost:8081");

                    DomainApiProperties apiProps = context.getBean(DomainApiProperties.class);
                    assertThat(apiProps.getOptionalApi("{@apim.resource}")).isPresent();
                    String resolved = apiProps.getUri("http://base/{@apim.resource}");
                    assertThat(resolved).isEqualTo("http://base/{@apim-pv}/resource?statusCode={statusCode}");
                });
    }
}
