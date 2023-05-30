package com.isel.ps.gateway.service

import com.isel.ps.gateway.db.ClientRoleRepository
import com.isel.ps.gateway.model.GatewayEntities.Companion.ClientRole
import org.springframework.stereotype.Service

@Service
class ClientRoleService(private val clientRoleRepository: ClientRoleRepository) {
    fun createClientRole(clientRole: ClientRole): ClientRole {
        validateClientRole(clientRole)
        return clientRoleRepository.create(clientRole)
    }

    private fun validateClientRole(clientRole: ClientRole) {
        if (clientRole.role.isBlank()) {
            throw IllegalArgumentException("Role must not be blank.")
        }
    }

    // ... other service methods ...
}