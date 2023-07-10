package com.isel.ps.gateway.config

import com.isel.ps.gateway.service.HubService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Component

@Component
class ShutdownConfig(val hubConfig: HubConfig, val hubService: HubService) : DisposableBean {
    private val logger: Logger = LoggerFactory.getLogger(ShutdownConfig::class.java)
    override fun destroy() {
        logger.info("Gateway[${hubConfig.getGateway().hubId}] shutdown received")
        deactivateGateway()
    }

    private fun deactivateGateway() {
        hubService.updateActiveHub(false, hubConfig.getGateway().hubId)
    }
}