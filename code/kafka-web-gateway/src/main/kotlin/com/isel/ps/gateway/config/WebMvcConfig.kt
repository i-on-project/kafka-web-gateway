package com.isel.ps.gateway.config

import com.isel.ps.gateway.auth.HandlerAuthenticationInterceptor
import com.isel.ps.gateway.logging.LogInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(
    private val authenticationInterceptor: HandlerAuthenticationInterceptor, val logInterceptor: LogInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(logInterceptor)
        registry.addInterceptor(authenticationInterceptor)
            .addPathPatterns("/api/**")
    }
}
