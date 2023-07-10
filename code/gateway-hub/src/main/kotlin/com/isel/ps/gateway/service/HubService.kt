package com.isel.ps.gateway.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.isel.ps.gateway.Utils
import com.isel.ps.gateway.db.HubRepository
import com.isel.ps.gateway.kafka.KafkaAdminUtil
import com.isel.ps.gateway.model.Hub
import com.isel.ps.gateway.model.SystemHub
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.config.TopicConfig
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant
import kotlin.random.Random

@Service
class HubService(
    private val hubRepository: HubRepository,
    private val kafkaAdminUtil: KafkaAdminUtil,
    private val producer: KafkaProducer<String, String>
) {

    private val mapper = jacksonObjectMapper()
    fun createHub(): Hub {
        val hubId: Long = generateHubId()

        val clientsTopic: String = generateHubTopic(hubId, "clients")
        val commandsTopic: String = generateHubTopic(hubId, "commands")
        val keysTopic: String = generateHubTopic(
            hubId,
            "keys",
            mapOf(Pair(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT))
        )
        val systemTopic = Utils.SYSTEM_TOPIC

        val hub = Hub(
            hubId,
            keysTopic,
            clientsTopic,
            commandsTopic,
            true,
            Timestamp.from(Instant.now())
        )
        attemptCreateSystemTopic(systemTopic)
        notifyArrivalToSystem(systemTopic, hub)

        hubRepository.create(hub)
        return hub
    }

    private fun notifyArrivalToSystem(systemTopic: String, hub: Hub) {
        producer.send(
            ProducerRecord(
                systemTopic, Utils.SystemKeys.NEW_SYSTEM_GATEWAY, mapper.writeValueAsString(
                    SystemHub("${hub.hubId}", hub.topicKeys, hub.topicClients, hub.topicCommands)
                )
            )
        )
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

    fun updateActiveHub(active: Boolean, hubId: Long) {
        hubRepository.updateActiveHub(active, hubId)
    }

    private fun generateHubId(): Long {
        var hubId = Random.nextLong(Long.MAX_VALUE)
        while (hubRepository.getById(hubId) != null) {
            hubId = Random.nextLong()
        }
        return hubId
    }

    private fun generateHubTopic(hubId: Long, topic: String, configs: Map<String, String>? = null): String {
        val topicHub = "hub-$hubId-$topic"
        kafkaAdminUtil.createTopic(topicHub, 3, 2, configs)
        return topicHub
    }
}