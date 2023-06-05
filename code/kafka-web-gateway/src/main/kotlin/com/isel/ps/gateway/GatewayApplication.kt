package com.isel.ps.gateway

import com.isel.ps.gateway.config.ShutdownConfig
import com.isel.ps.gateway.service.GatewayService
import com.isel.ps.gateway.utils.Sha256TokenEncoder
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@SpringBootApplication
class GatewayApplication(val gatewayService: GatewayService) {
    @Bean
    fun shutdownHandler(): ShutdownConfig {
        return ShutdownConfig(gatewayService)
    }

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun tokenEncoder() = Sha256TokenEncoder()
}

fun main(args: Array<String>) {
    runApplication<GatewayApplication>(*args)
}
