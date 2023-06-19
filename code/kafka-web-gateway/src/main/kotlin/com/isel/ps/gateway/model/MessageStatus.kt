package com.isel.ps.gateway.model

import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


@Component
class MessageStatus {
    private val messageStatuses: ConcurrentMap<String, ConcurrentMap<String, MessageInfo>>

    init {
        messageStatuses = ConcurrentHashMap()
    }

    fun getMessageStatuses(): ConcurrentMap<String, ConcurrentMap<String, MessageInfo>> {
        return messageStatuses
    }
}

data class MessageInfo(val timestamp: Instant, val isAcked: Boolean)