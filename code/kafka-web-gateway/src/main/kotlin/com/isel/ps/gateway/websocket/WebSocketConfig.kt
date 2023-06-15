package com.isel.ps.gateway.websocket

import com.isel.ps.gateway.db.SettingRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.server.HandshakeInterceptor

@Configuration
@EnableWebSocket
class WebSocketConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    private val settingRepository: SettingRepository,
    private val gatewayWebsocketHandler: GatewayWebsocketHandler
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(gatewayWebsocketHandler, "/socket")
            .addInterceptors(gatewayAuthenticationInterceptor())
            .setAllowedOrigins("*")
    }

    @Bean
    fun gatewayAuthenticationInterceptor(): HandshakeInterceptor {
        return ClientAuthenticationInterceptor(settingRepository)
    }

}
