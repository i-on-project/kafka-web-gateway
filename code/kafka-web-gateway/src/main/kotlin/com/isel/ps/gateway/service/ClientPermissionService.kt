package com.isel.ps.gateway.service

import com.isel.ps.gateway.db.ClientPermissionRepository
import com.isel.ps.gateway.model.GatewayEntities.Companion.ClientPermission
import org.springframework.stereotype.Service

@Service
class ClientPermissionService(private val clientPermissionRepository: ClientPermissionRepository) {
    fun createClientPermission(clientPermission: ClientPermission): ClientPermission {
        validateClientPermission(clientPermission)
        clientPermission.permissionId = clientPermissionRepository.create(clientPermission)
        return clientPermission
    }

    private fun validateClientPermission(clientPermission: ClientPermission) {
        if (clientPermission.topic.isBlank()) {
            throw IllegalArgumentException("Topic must not be blank.")
        }
    }
}