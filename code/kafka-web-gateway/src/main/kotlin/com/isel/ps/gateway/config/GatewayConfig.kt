package com.isel.ps.gateway.config

import com.isel.ps.gateway.model.Gateway
import com.isel.ps.gateway.service.GatewayService
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class GatewayConfig(private val gatewayService: GatewayService) {
    private val logger: Logger = LoggerFactory.getLogger(GatewayConfig::class.java)
    private lateinit var gateway: Gateway

    @PostConstruct
    fun initialize() {
        gateway = gatewayService.createGateway()
        logger.info("Gateway with topics \"${gateway.topicClients}\" and \"${gateway.topicCommands}\" created.")
    }

    fun getGateway(): Gateway {
        return gateway
    }
}
