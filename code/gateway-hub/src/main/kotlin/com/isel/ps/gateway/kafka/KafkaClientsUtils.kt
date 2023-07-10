package com.isel.ps.gateway.kafka

import com.isel.ps.gateway.config.HubConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class KafkaClientsUtils(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String,
    private val hubConfig: HubConfig
) {
    companion object {

    }

    fun getConsumerDefaultProperties(groupId: String = "gateway-${hubConfig.getGateway().hubId}"): Properties { // TODO: change the way this groupId is created + default value
        val props = Properties()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        return props
    }
}