package com.isel.ps.gateway.model

import java.sql.Timestamp

class GatewayEntities {
    companion object {
        data class Client(
            val clientId: Long,
            val username: String
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
            val createdAt: Timestamp
        )

        data class ActiveSession(
            val sessionId: Long,
            val updatedAt: Timestamp
        )

        data class Subscription(
            val subscriptionId: Int,
            val sessionId: Long,
            val topic: String,
            val key: String?
        )

        data class ClientRole(
            val roleId: Int?,
            val role: String,
            val description: String?
        )

        data class ClientRoleAssignment(
            val clientId: Long,
            val roleId: Int
        )

        data class ClientPermission(
            var permissionId: Int,
            val topic: String,
            val key: String?,
            val read: Boolean,
            val write: Boolean
        )

        data class ClientRolePermission(
            val roleId: Int,
            val permissionId: Int
        )

        data class Admin(
            val adminId: Int?,
            val username: String?,
            var passwordValidation: String,
            val description: String?,
            val owner: Boolean
        )

        data class AdminRole(
            val roleId: Int?,
            val role: String,
            val description: String?
        )

        data class AdminRoleAssignment(
            val adminId: Int,
            val roleId: Int
        )

        data class AdminPermission(
            val permissionId: Int?,
            val administrative: Boolean,
            val clientPermission: Boolean
        )

        data class AdminRolePermission(
            val roleId: Int,
            val permissionId: Int
        )

        data class AdminToken(
            val tokenValidation: String,
            val adminId: Int,
            val createdAt: Timestamp,
            val lastUsedAt: Timestamp
        )

        data class Setting(
            val settingName: String,
            var settingValue: String,
            val settingDescription: String,
            val updatedAt: Timestamp
        )
    }
}