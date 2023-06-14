package com.isel.kafkastreamsmoduledemo.recordRouter

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class RRTesting(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String
) {
    fun test() {
        val props = Properties()
        props["bootstrap.servers"] = bootstrapServers
        props["key.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
        props["value.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
        val producer: KafkaProducer<String, String> = KafkaProducer<String, String>(props)

        val systemTopic: String = "SYSTEM_TOPIC"


        //producer.send(ProducerRecord())


    }
}