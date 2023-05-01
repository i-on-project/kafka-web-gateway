package com.isel.ps.gateway.websocket

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.web.socket.*
import java.time.Duration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import org.slf4j.Logger

class MyWebSocketHandler(
        private val kafkaProducer: KafkaProducer<String, String>,
        private val kafkaConsumer: KafkaConsumer<String, String>
) : WebSocketHandler {

    val log: Logger = LoggerFactory.getLogger(MyWebSocketHandler::class.java)
    var executor: ExecutorService = Executors.newFixedThreadPool(2)

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        log.debug("Began handleMessage.")
        val payload = message.payload as String
        if (payload.startsWith("subscribe:")) {
            log.debug("handleMessage subscribe.")
            val topic = payload.substringAfter("subscribe:")
            kafkaConsumer.subscribe(listOf(topic))
            executor.submit {
                while (true) {
                    val records = kafkaConsumer.poll(Duration.ofSeconds(1))

                    for (record in records) {
                        session.sendMessage(TextMessage(record.value()))
                    }
                }
            }
        } else if (payload.startsWith("publish:")) {
            log.debug("handleMessage publish.")
            val parts = payload.substringAfter("publish:").split(",")
            val topic = parts[0]
            val key = parts[1]
            val data = parts[2]
            log.debug("handleMessage publish. Topic[${topic}] Key[${key}] Data[${data}].")
            kafkaProducer.send(ProducerRecord(topic, key, data))
        }
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
    }

    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {

    }

    override fun supportsPartialMessages(): Boolean {
        return false
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {}
}