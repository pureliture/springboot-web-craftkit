package com.teststrategy.multimodule.maven.config

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration


@ComponentScan(basePackages = ["sf.framework.hystrix"])
@Configuration
@EnableAutoConfiguration
open class HystrixTestConfig {
}