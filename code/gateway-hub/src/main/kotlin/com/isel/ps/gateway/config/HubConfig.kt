package com.isel.ps.gateway.config

import com.isel.ps.gateway.model.Hub
import com.isel.ps.gateway.service.HubService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class HubConfig(private val hubService: HubService) {
    private val logger: Logger = LoggerFactory.getLogger(HubConfig::class.java)
    private lateinit var hub: Hub

    @PostConstruct
    fun initialize() {
        hub = hubService.createHub()
        logger.info("Gateway with topics \"${hub.topicClients}\" and \"${hub.topicCommands}\" created.")
    }

    fun getGateway(): Hub {
        return hub
    }
}
