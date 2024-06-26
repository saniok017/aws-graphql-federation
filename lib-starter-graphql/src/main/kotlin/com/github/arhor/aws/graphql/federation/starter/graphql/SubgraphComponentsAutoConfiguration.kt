package com.github.arhor.aws.graphql.federation.starter.graphql

import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver

@ComponentScan
@AutoConfiguration(before = [DgsAutoConfiguration::class])
class SubgraphComponentsAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(AuthenticationPrincipalArgumentResolver::class)
    class AuthenticationPrincipalDgsArgumentResolverConfig {
        @Bean
        @ConditionalOnMissingBean
        fun authenticationPrincipalDgsArgumentResolver(): AuthenticationPrincipalDgsArgumentResolver {
            return AuthenticationPrincipalDgsArgumentResolver()
        }
    }
}
