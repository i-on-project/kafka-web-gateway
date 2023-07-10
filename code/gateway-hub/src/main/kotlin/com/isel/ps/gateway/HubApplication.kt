package com.isel.ps.gateway

import com.isel.ps.gateway.config.HubConfig
import com.isel.ps.gateway.config.ShutdownConfig
import com.isel.ps.gateway.service.HubService
import com.isel.ps.gateway.utils.Sha256TokenEncoder
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class HubApplication(val hubService: HubService, val hubConfig: HubConfig) {
    @Bean
    fun shutdownHandler(): ShutdownConfig {
        return ShutdownConfig(hubConfig, hubService)
    }

    @Bean
    fun tokenEncoder() = Sha256TokenEncoder()
}

fun main(args: Array<String>) {
    runApplication<HubApplication>(*args)
}
