package com.isel.ps.gateway.config

import com.isel.ps.gateway.config.GatewayConfig.Companion.GATEWAY
import com.isel.ps.gateway.service.GatewayService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Component

@Component
class ShutdownConfig(val gatewayService: GatewayService) : DisposableBean {
    private val logger: Logger = LoggerFactory.getLogger(ShutdownConfig::class.java)
    override fun destroy() {
        logger.info("Gateway[${GATEWAY?.gatewayId}] shutdown received")
        deactivateGateway()
    }

    private fun deactivateGateway() {
        if (GATEWAY == null) {
            logger.warn("Gateway instance not initialized. Ignoring deactivation")
            return
        }

        gatewayService.updateActiveGateway(false, GATEWAY!!.gatewayId)
    }
}