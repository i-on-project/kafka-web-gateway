package com.isel.ps.gateway.websocket

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.web.socket.*
import java.time.Duration

class MyWebSocketHandler(
        private val kafkaProducer: KafkaProducer<String, String>,
        private val kafkaConsumer: KafkaConsumer<String, String>
) : WebSocketHandler {

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        val payload = message.payload as String
        if (payload.startsWith("subscribe:")) {
            val topic = payload.substringAfter("subscribe:")
            kafkaConsumer.subscribe(listOf(topic))
            while (true) {
                val records = kafkaConsumer.poll(Duration.ofSeconds(1))
                for (record in records) {
                    session.sendMessage(TextMessage(record.value()))
                }
            }
        } else if (payload.startsWith("publish:")) {
            val parts = payload.substringAfter("publish:").split(",")
            val topic = parts[0]
            val key = parts[1]
            val data = parts[2]
            kafkaProducer.send(ProducerRecord(topic, key, data))
        }
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        TODO("Not yet implemented")
    }

    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        TODO("Not yet implemented")

    }

    override fun supportsPartialMessages(): Boolean {
        TODO("Not yet implemented")
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {}
}