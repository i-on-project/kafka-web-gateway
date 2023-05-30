package com.isel.ps.gateway

import com.isel.ps.gateway.config.ShutdownHandler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class GatewayApplication(val gatewayService: GatewayService) {
    @Bean
    fun shutdownHandler(): ShutdownHandler {
        return ShutdownHandler(gatewayService)
    }
}

fun main(args: Array<String>) {
    runApplication<GatewayApplication>(*args)
}
