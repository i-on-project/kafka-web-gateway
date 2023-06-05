package com.isel.ps.gateway.config

import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Component

@Component
class ShutdownConfig(gatewayService: GatewayService) : DisposableBean {
    override fun destroy() {
        // TODO Update the gateway table to set active = false
    }
}