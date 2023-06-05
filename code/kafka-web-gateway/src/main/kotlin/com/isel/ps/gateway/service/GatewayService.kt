package com.isel.ps.gateway.service

import com.isel.ps.gateway.db.GatewayRepository
import com.isel.ps.gateway.model.GatewayEntities.Companion.Gateway
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant
import java.util.*
import kotlin.random.Random

@Service
class GatewayService(private val gatewayRepository: GatewayRepository) {
    fun createGateway(owner: Boolean = false): Gateway {
        val gatewayId = generateGatewayId()

        val gateway = Gateway(
            gatewayId,
            generateGatewayTopic(gatewayId, "clients"),
            generateGatewayTopic(gatewayId, "commands"),
            owner,
            Timestamp.from(Instant.now())
        )

        gatewayRepository.create(gateway)
        return gateway
    }

    private fun generateGatewayId(): Long {
        var gatewayId = Random.nextLong()
        while (gatewayRepository.getById(gatewayId) != null) {
            gatewayId = Random.nextLong()
        }
        return gatewayId
    }

    private fun generateGatewayTopic(gatewayId: Long, topic: String): String {
        return "$gatewayId-$topic-${UUID.randomUUID()}"
    }
}