package com.isel.ps.gateway.service

import com.isel.ps.gateway.db.GatewayRepository
import com.isel.ps.gateway.kafka.KafkaAdminUtil
import com.isel.ps.gateway.model.Gateway
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant
import java.util.*
import kotlin.random.Random

@Service
class GatewayService(private val gatewayRepository: GatewayRepository, private val kafkaAdminUtil: KafkaAdminUtil) {
    fun createGateway(): Gateway {
        val gatewayId = generateGatewayId()

        val gateway = Gateway(
            gatewayId,
            generateGatewayTopic(gatewayId, "clients"),
            generateGatewayTopic(gatewayId, "commands"),
            true,
            Timestamp.from(Instant.now())
        )

        gatewayRepository.create(gateway)
        return gateway
    }

    fun updateActiveGateway(active: Boolean, gatewayId: Long) {
        gatewayRepository.updateActiveGateway(active, gatewayId)
    }

    private fun generateGatewayId(): Long {
        var gatewayId = Random.nextLong(Long.MAX_VALUE)
        while (gatewayRepository.getById(gatewayId) != null) {
            gatewayId = Random.nextLong()
        }
        return gatewayId
    }

    private fun generateGatewayTopic(gatewayId: Long, topic: String): String {
        val topicGateway = "$gatewayId-$topic-${UUID.randomUUID()}"
        kafkaAdminUtil.createTopic(topicGateway, 3, 2)
        return topicGateway
    }
}