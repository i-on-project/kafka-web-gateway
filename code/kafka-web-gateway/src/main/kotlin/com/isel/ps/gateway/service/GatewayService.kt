package com.isel.ps.gateway.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.isel.ps.gateway.Utils
import com.isel.ps.gateway.db.GatewayRepository
import com.isel.ps.gateway.kafka.KafkaAdminUtil
import com.isel.ps.gateway.model.Gateway
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.config.Config
import org.apache.kafka.common.config.TopicConfig
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant
import java.util.*
import kotlin.random.Random

@Service
class GatewayService(
    private val gatewayRepository: GatewayRepository,
    private val kafkaAdminUtil: KafkaAdminUtil,
    private val producer: KafkaProducer<String, String>) {

    private val mapper = jacksonObjectMapper()
    fun createGateway(): Gateway {
        val gatewayId: Long = generateGatewayId()

        val clientsTopic: String = generateGatewayTopic(gatewayId, "clients")
        val commandsTopic: String = generateGatewayTopic(gatewayId, "commands")
        val keysTopic: String = generateGatewayTopic(
            gatewayId,
            "keys",
            mapOf(Pair(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT))
        )
        val systemTopic = Utils.SYSTEM_TOPIC

        val gateway = Gateway(
            gatewayId,
            keysTopic,
            clientsTopic,
            commandsTopic,
            true,
            Timestamp.from(Instant.now())
        )
        attemptCreateSystemTopic(systemTopic)
        notifyArrivalToSystem(systemTopic, gateway)

        gatewayRepository.create(gateway)
        return gateway
    }

    private fun notifyArrivalToSystem(systemTopic: String, gateway: Gateway) {
        producer.send(ProducerRecord(systemTopic, Utils.SystemKeys.NEW_SYSTEM_GATEWAY, mapper.writeValueAsString(GatewayKafka.SystemGateway("${gateway.gatewayId}", gateway.topicKeys, gateway.topicClients, gateway.topicCommands))))
    }

    private fun attemptCreateSystemTopic(systemTopic: String) {
        try {
            if (!kafkaAdminUtil.hasTopic(systemTopic)) {
                kafkaAdminUtil.createTopic(systemTopic, 3, 3)
                print("Created $systemTopic")
            }
        } catch (ex: Exception) {
            println("Failed attemptCreateSystemTopic ex:${ex.message}")
        }
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

    private fun generateGatewayTopic(gatewayId: Long, topic: String, configs: Map<String, String>? = null): String {
        val topicGateway = "$gatewayId-$topic-${UUID.randomUUID()}"
        kafkaAdminUtil.createTopic(topicGateway, 3, 2, configs)
        return topicGateway
    }
}