package com.isel.ps.gateway.utils

import org.slf4j.Logger
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import java.util.*

class SendTask(
    private val messageStatuses: Map<String, Map<String, Boolean>>,
    private val userId: String,
    private val messageId: String,
    private val remainingRetries: Int,
    private val session: WebSocketSession,
    private val textMessage: WebSocketMessage<*>,
    private val executor: Timer,
    private val logger: Logger
) : TimerTask() {
    override fun run() {
        if (messageStatuses[userId]?.get(messageId) != true && remainingRetries > 0) {
            session.sendMessage(textMessage)
            rescheduleTask(remainingRetries - 1)
        } else {
            // Handle when the message was acknowledged or retries are exhausted
            if (remainingRetries == 0) {
                // Retry limit reached
                logger.info("Message retries exhausted")
            } else {
                // Message acknowledged
                logger.info("Message acknowledged")
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
            executor,
            logger
        )
        executor.schedule(newTask, 10000L)
    }
}