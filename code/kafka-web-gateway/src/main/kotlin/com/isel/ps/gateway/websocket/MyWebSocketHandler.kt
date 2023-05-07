package com.isel.ps.gateway.websocket

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.isel.ps.gateway.model.GatewayTypes.*
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.utils.Utils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.socket.*
import java.nio.charset.Charset
import java.time.Duration
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MyWebSocketHandler(
    private val kafkaProducer: KafkaProducer<String, String>,
    private val kafkaConsumer: KafkaConsumer<String, String>,
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String
) : WebSocketHandler {

    val log: Logger = LoggerFactory.getLogger(MyWebSocketHandler::class.java)
    var executor: ExecutorService = Executors.newFixedThreadPool(2)
    val subscriptions: MutableMap<String, List<TopicType>> = mutableMapOf()

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        log.debug("Began handleMessage.")
        val payload = message.payload as String
        // Assuming jsonStr is a string containing a valid JSON object representing a command
        val objectMapper = jacksonObjectMapper()
        val clientMessage = objectMapper.readValue(payload, ClientMessage::class.java)

        when (clientMessage.command.type) {
            "subscribe" -> {
                val subscribeCommand = objectMapper.convertValue(clientMessage.command, Subscribe::class.java)
                subscribeCommand.topics.forEach {topic ->
                    val props = Properties()
                    props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
                    props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
                    props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
                    // props[ConsumerConfig.GROUP_ID_CONFIG] = "my-group"
                    subscriptions[topic.topic] = kafkaConsumer.subscribe(props)
                }
                // Do something with the subscribe command
            }

            "consume" -> {
                val consumeCommand = objectMapper.convertValue(clientMessage.command, Consume::class.java)
                // Do something with the consume command
            }

            "publish" -> {
                val publishCommand = objectMapper.convertValue(clientMessage.command, Publish::class.java)
                // Do something with the publish command
            }

            "pause" -> {
                val pauseCommand = objectMapper.convertValue(clientMessage.command, Pause::class.java)
                // Do something with the pause command
            }

            "resume" -> {
                val resumeCommand = objectMapper.convertValue(clientMessage.command, Resume::class.java)
                // Do something with the resume command
            }

            else -> {
                // Handle unknown command type
            }
        }


        if (payload.startsWith("subscribe:")) {
            log.debug("handleMessage subscribe.")
            val topic = payload.substringAfter("subscribe:")
            // Utils.toPositive(Utils.murmur2(topic.toByteArray())) % numPartitions;

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