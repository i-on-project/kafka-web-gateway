package com.isel.ps.gateway.utils

import com.isel.ps.gateway.model.MessageInfo
import com.isel.ps.gateway.websocket.ClientAuthenticationInterceptor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

fun ConcurrentWebSocketSessionDecorator.sendMessage(
    messageId: String,
    textMessage: WebSocketMessage<*>,
    messageStatuses: ConcurrentMap<String, ConcurrentMap<String, MessageInfo>>,
    messageExecutor: ScheduledExecutorService,
    retries: Int = 3
) {
    val session = this
    val userId = session.attributes[ClientAuthenticationInterceptor.CLIENT_ID] as String
    // Create a nested map for each user if it doesn't exist
    messageStatuses.putIfAbsent(userId, ConcurrentHashMap())

    messageStatuses[userId]!![messageId] = MessageInfo(Instant.now(), false)

    val sendTask = SendTask(
        messageStatuses,
        userId,
        messageId,
        retries,
        session,
        textMessage,
        messageExecutor,
    )

    session.sendMessage(textMessage)
    messageExecutor.schedule(sendTask, 10000L, TimeUnit.MILLISECONDS)
}

class SendTask(
    private val messageStatuses: ConcurrentMap<String, ConcurrentMap<String, MessageInfo>>,
    private val userId: String,
    private val messageId: String,
    private val remainingRetries: Int,
    private val session: WebSocketSession,
    private val textMessage: WebSocketMessage<*>,
    private val executor: ScheduledExecutorService
) : TimerTask() {
    private val logger: Logger = LoggerFactory.getLogger(SendTask::class.java)
    override fun run() {
        if (messageStatuses[userId]?.get(messageId)?.isAcked == false && remainingRetries > 0) {
            session.sendMessage(textMessage)
            rescheduleTask(remainingRetries - 1)
        } else {
            // Handle when the message was acknowledged or retries are exhausted
            if (remainingRetries == 0) {
                // Retry limit reached
                logger.info("[$messageId] retries exhausted")
            } else {
                // Message acknowledged
                logger.info("[$messageId] acknowledged")
            }
        }
    }

    private fun rescheduleTask(retries: Int) {
        val newTask = SendTask(
            messageStatuses,
            userId,
            messageId,
            retries,
            session,
            textMessage,
            executor
        )

        executor.schedule(newTask, 10000L, TimeUnit.MILLISECONDS)
    }
}