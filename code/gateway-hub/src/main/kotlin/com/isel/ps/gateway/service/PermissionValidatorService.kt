package com.isel.ps.gateway.service

import com.isel.ps.gateway.db.ClientPermissionRepository
import com.isel.ps.gateway.db.ClientRoleRepository
import com.isel.ps.gateway.db.RolePermissionRepository
import com.isel.ps.gateway.model.TopicType
import org.springframework.stereotype.Service

@Service
class PermissionValidatorService(
    private val clientPermissionRepository: ClientPermissionRepository,
    private val rolePermissionRepository: RolePermissionRepository,
    private val clientRoleRepository: ClientRoleRepository
) {
    fun hasPermission(topic: String, key: String?, clientId: String, readPermission: Boolean): Boolean {
        // Check if the client has direct permission
        if (clientPermissionRepository.hasPermission(clientId, topic, key, readPermission)) {
            return true
        }

        // Check if the client has permission through roles
        val clientRoles = clientRoleRepository.getAll()
        for (clientRole in clientRoles) {
            val roleId = clientRole.roleId
            if (rolePermissionRepository.hasPermission(roleId, topic, key, readPermission)) {
                return true
            }
        }

        return false
    }

    fun hasPermissionForAll(topics: List<TopicType>, clientId: String, readPermission: Boolean): Boolean {
        topics.forEach {
            if (!hasPermission(it.topic, it.key, clientId, readPermission))
                return false
        }

        return true
    }
}