package com.isel.ps.gateway.kafka

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class KafkaConfig(
        @Value("\${spring.kafka.bootstrap-servers}")
        private val bootstrapServers: String
) {

    @Bean
    fun kafkaProducer(): KafkaProducer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = bootstrapServers
        props["key.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
        props["value.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
        return KafkaProducer(props)
    }

    @Bean
    fun kafkaConsumer(): KafkaConsumer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = bootstrapServers
        props["key.deserializer"] = "org.apache.kafka.common.serialization.StringDeserializer"
        props["value.deserializer"] = "org.apache.kafka.common.serialization.StringDeserializer"
        props["group.id"] = "my-group"
        return KafkaConsumer(props)
    }
}