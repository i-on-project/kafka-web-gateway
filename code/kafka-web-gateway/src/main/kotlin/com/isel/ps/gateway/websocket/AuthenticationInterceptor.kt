package com.isel.ps.gateway.websocket

import com.isel.ps.gateway.auth.RestAuthenticationProvider
import com.isel.ps.gateway.model.USER_ID
import org.springframework.http.HttpStatusCode
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import org.springframework.web.util.UriComponentsBuilder


class AuthenticationInterceptor : HandshakeInterceptor {
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
            println("No token $request.uri")
            response.setStatusCode(HttpStatusCode.valueOf(400))
            return false;
        }

        println("token: $authorizationToken")

        val authenticationProvider = RestAuthenticationProvider("http://mockbin.org/bin/1df228cc-b7cb-4dda-aeba-2c21e1070c02?foo=bar&foo=baz")
        val userId = authenticationProvider.validateToken(authorizationToken)

        if (userId == null) {
            response.setStatusCode(HttpStatusCode.valueOf(401))
            println("NOT AUTHORIZED $response")
            return false;
        }

        // Add the authenticated user to the attributes of the WebSocket session
        attributes[USER_ID] = userId;
        println("AUTHORIZED: $userId")
        return true;
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) {
        // TODO("Not yet implemented")
    }
}