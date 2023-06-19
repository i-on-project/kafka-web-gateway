package com.isel.ps.gateway.websocket

import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator
import java.util.concurrent.ConcurrentHashMap

data class ClientSession(
    val id: String,
    val session: ConcurrentWebSocketSessionDecorator
)
@Component
class ClientSessions {

    // TODO: Why in companion object?
    companion object {
        // Never edit values of map
        private val sessions: ConcurrentHashMap<String, ClientSession> = ConcurrentHashMap()
    }

    fun addSession(session: ConcurrentWebSocketSessionDecorator) {
        val clientSession = ClientSession(session.id, session)
        sessions.put(clientSession.id, clientSession)
    }

    fun getSession(sessionId: String): ConcurrentWebSocketSessionDecorator? {
        return sessions.get(sessionId)?.session
    }

    fun getClientSession(clientSessionId: String): ClientSession? {
        return sessions.get(clientSessionId)
    }

    fun removeSession(clientSessionId: String): Boolean {
        return sessions.remove(clientSessionId) != null
    }
}