package com.isel.ps.gateway.service

import com.isel.ps.gateway.db.ClientRolePermissionRepository
import com.isel.ps.gateway.model.GatewayEntities.Companion.ClientRolePermission
import org.springframework.stereotype.Service

@Service
class ClientRolePermissionService(private val clientRolePermissionRepository: ClientRolePermissionRepository) {
    fun createClientRolePermission(clientRolePermission: ClientRolePermission): ClientRolePermission {
        return clientRolePermissionRepository.create(clientRolePermission)
    }
}