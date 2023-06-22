package com.isel.ps.gateway.utils

import com.isel.ps.gateway.model.MessageInfo
import com.isel.ps.gateway.model.MessageStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentMap

/**
 * Responsible for purging old messages that are yet to be removed.
 * Should only happen if a client is connected for a long time
 */
@Component
class MessageStatusManager(private val messageStatus: MessageStatus) {

    /**
     * Scheduled every 5 minutes.
     */
    @Scheduled(fixedRate = 300000) // TODO: Test this, check if it is even working/scheduled.
    fun purgeOldMessages() {
        val messageStatuses: ConcurrentMap<String, ConcurrentMap<String, MessageInfo>> =
            messageStatus.getMessageStatuses()
        for (clientKey in messageStatuses.keys) {
            val clientStatus: ConcurrentMap<String, MessageInfo> = messageStatuses[clientKey]!!
            for (messageId in clientStatus.keys) {
                if (shouldPurgeMessage(clientKey, messageId)) {
                    clientStatus.remove(messageId)
                }
            }
        }
    }

    private fun shouldPurgeMessage(clientKey: String, messageId: String): Boolean {
        val messageInfo: MessageInfo? = messageStatus.getMessageStatuses()[clientKey]!![messageId]
        if (messageInfo != null) {
            val messageTimestamp: Instant = messageInfo.timestamp
            val currentTimestamp = Instant.now()
            val age: Duration = Duration.between(messageTimestamp, currentTimestamp)
            // Remove messages older than 1 minutes
            return age > Duration.ofMinutes(1)
        }
        return false
    }
}