package com.github.arhor.dgs.users.test

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration(proxyBeanMethods = false)
class TestObjectMapperConfig {

    @Bean
    fun objectMapper() = jacksonObjectMapper()
}
