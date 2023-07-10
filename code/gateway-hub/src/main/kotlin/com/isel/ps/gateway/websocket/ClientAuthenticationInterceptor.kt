package com.isel.ps.gateway.websocket

import com.isel.ps.gateway.auth.HttpAuthenticationProvider
import com.isel.ps.gateway.db.SettingRepository
import com.isel.ps.gateway.model.SettingType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import org.springframework.web.util.UriComponentsBuilder


class ClientAuthenticationInterceptor(private val settingRepository: SettingRepository) : HandshakeInterceptor {

    private val logger: Logger = LoggerFactory.getLogger(ClientAuthenticationInterceptor::class.java)

    companion object {
        const val CLIENT_ID: String = "client_id"
    }

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {

        val uriBuilder = UriComponentsBuilder.fromUri(request.uri)
        val queryParams = uriBuilder.build().queryParams.toSingleValueMap()

        val authorizationToken = queryParams["token"]
        if (authorizationToken == null) {
            logger.warn("No token received in $request.uri")
            response.setStatusCode(HttpStatus.BAD_REQUEST)
            return false
        }

        // TODO: Use cache to prevent several DB queries
        val authEndpoint = settingRepository.getBySettingName(SettingType.AuthServer.settingName)

        if (authEndpoint?.name == null) {
            logger.warn("Authentication Server not configured, unable to authenticate clients.")
            return false
        }

        val authenticationProvider = HttpAuthenticationProvider(authEndpoint.value)
        val client = authenticationProvider.validateToken(authorizationToken)

        if (client == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED)
            logger.warn("Unauthorized client with $authorizationToken token.")

            return false
        }

        // Add the authenticated user to the attributes of the WebSocket session
        attributes[CLIENT_ID] = client.clientId
        return true
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) {
        // Not needed
    }
}