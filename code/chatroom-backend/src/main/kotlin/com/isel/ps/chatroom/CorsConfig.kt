package com.isel.ps.chatroom

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class CorsConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**") // Apply CORS configuration to all endpoints
            .allowedOriginPatterns("*") // Allow requests from any origin. Replace "*" with the specific origins you want to allow
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*") // Allow all headers
            .allowCredentials(true) // Allow sending credentials such as cookies
    }
}