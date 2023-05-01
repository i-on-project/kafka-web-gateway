package com.isel.ps.gateway.websocket

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(private val kafkaProducer: KafkaProducer<String, String>,
                      private val kafkaConsumer: KafkaConsumer<String, String>) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(myWebSocketHandler(), "/socket")
                .setAllowedOrigins("*")
    }

    @Bean
    fun myWebSocketHandler(): WebSocketHandler {
        return MyWebSocketHandler(kafkaProducer, kafkaConsumer)
    }

}
