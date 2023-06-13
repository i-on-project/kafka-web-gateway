package com.isel.ps.gateway

import com.isel.ps.gateway.config.GatewayConfig
import com.isel.ps.gateway.config.ShutdownConfig
import com.isel.ps.gateway.service.GatewayService
import com.isel.ps.gateway.utils.Sha256TokenEncoder
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class GatewayApplication(val gatewayService: GatewayService, val gatewayConfig: GatewayConfig) {
    @Bean
    fun shutdownHandler(): ShutdownConfig {
        return ShutdownConfig(gatewayConfig, gatewayService)
    }

    @Bean
    fun tokenEncoder() = Sha256TokenEncoder()
}

fun main(args: Array<String>) {
    runApplication<GatewayApplication>(*args)
}
