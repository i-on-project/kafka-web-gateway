package com.isel.ps.gateway.websocket

import com.isel.ps.gateway.db.SettingRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.server.HandshakeInterceptor

@Configuration
@EnableScheduling
@EnableWebSocket
class WebSocketConfig(
    private val settingRepository: SettingRepository,
    private val hubWebsocketHandler: HubWebsocketHandler
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(hubWebsocketHandler, "/socket")
            .addInterceptors(gatewayAuthenticationInterceptor())
            .setAllowedOrigins("*")
    }

    @Bean
    fun gatewayAuthenticationInterceptor(): HandshakeInterceptor {
        return ClientAuthenticationInterceptor(settingRepository)
    }

}
