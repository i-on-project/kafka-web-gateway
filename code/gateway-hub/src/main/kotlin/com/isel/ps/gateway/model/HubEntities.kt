package com.isel.ps.gateway.model

import java.sql.Timestamp
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

data class Client(
    val clientId: String
)

data class Hub(
    val hubId: Long,
    val topicKeys: String,
    val topicClients: String,
    val topicCommands: String,
    val active: Boolean,
    val updatedAt: Timestamp
)

data class Session(
    val sessionId: String,
    val clientId: String,
    val gatewayId: Long,
    val createdAt: Timestamp,
    val updatedAt: Timestamp,
    val active: Boolean
)

data class Subscription(
    val subscriptionId: Int,
    val sessionId: String,
    val topic: String,
    val key: String?
)

data class Role(
    val roleId: Int?,
    val name: String,
    val description: String?
)

data class ClientRole(
    val clientId: String,
    val roleId: Int
)

data class Permission(
    var permissionId: Int?,
    val topic: String,
    val key: String?,
    val read: Boolean,
    val write: Boolean
)

data class RolePermission(
    val roleId: Int,
    val permissionId: Int
)

data class ClientPermission(
    val clientId: String,
    val permissionId: Int
)

data class Admin(
    var adminId: Int?,
    val name: String,
    val description: String?,
    val owner: Boolean,
    val administrative: Boolean,
    val permission: Boolean
) {
    constructor() : this(null, "", "", false, false, false)
}

data class AdminToken(
    val tokenValidation: String,
    val adminId: Int,
    val createdAt: Timestamp?,
    val lastUsedAt: Timestamp?
)

data class Setting(
    val name: String,
    var value: String,
    var description: String?,
    val updatedAt: Timestamp
)

enum class SettingType(val settingName: String) {
    AuthServer("auth-server")
}

data class SystemHub(
    val hubId: String,
    val keysTopicName: String,
    val clientsTopicName: String,
    val commandsTopicName: String?
)

data class HubDetails(
    val hubId: String,
    val systemHub: SystemHub,
    val topicsKeys: ConcurrentHashMap<String, List<String>> = ConcurrentHashMap<String, List<String>>(), // TODO: Must disappear
    val fullTopics: AtomicReference<List<String>> = AtomicReference(listOf()) // TODO: Must disappear
)

data class HubTopicKeys(
    val keys: List<String>?,
    val allKeys: Boolean?
)

data class BooleanObj(
    var value: Boolean
)