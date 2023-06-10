package com.isel.ps.gateway.model

import java.sql.Timestamp

data class Client(
    val clientId: Long
)

data class Gateway(
    val gatewayId: Long,
    val topicClients: String,
    val topicCommands: String,
    val active: Boolean,
    val updatedAt: Timestamp
)

data class Session(
    val sessionId: Long,
    val clientId: Long,
    val gatewayId: Long,
    val createdAt: Timestamp,
    val updatedAt: Timestamp,
    val active: Boolean
)

data class Subscription(
    val subscriptionId: Int,
    val sessionId: Long,
    val topic: String,
    val key: String?
)

data class Role(
    val roleId: Int?,
    val name: String,
    val description: String?
)

data class ClientRole(
    val clientId: Long,
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
    val clientId: Long,
    val permissionId: Int
)

data class Admin(
    val adminId: Int?,
    val name: String,
    val description: String?,
    val owner: Boolean,
    val administrative: Boolean,
    val permission: Boolean
)

data class AdminToken(
    val tokenValidation: String,
    val adminId: Int,
    val createdAt: Timestamp?,
    val lastUsedAt: Timestamp
)

data class Setting(
    val name: String,
    var value: String,
    val description: String?,
    val updatedAt: Timestamp
)

enum class SettingType(val settingName: String) {
    AuthServer("auth-server")
}