package com.isel.ps.gateway.model

import java.sql.Timestamp

const val UPDATED_AT = "updated_at"
const val SESSION_ID = "session_id"
const val USER_ID = "user_id"
const val CREATED_AT = "created_at"
const val GATEWAY_TOPIC = "gateway_topic"


data class Session(
    val sessionId: Long,
    val userId: Long,
    val createdAt: Timestamp,
    val gatewayTopic: String
)

data class ActiveSession(
    val sessionId: Long,
    val updatedAt: Timestamp
)

data class Subscription(
    val sessionId: Long,
    val topic: String,
    val key: String
)

data class UserInfo(
    val userId: Long,
    val username: String
)

data class Permission(
    val userId: Long,
    val topic: String,
    val key: String?
)

