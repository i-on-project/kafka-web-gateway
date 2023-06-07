package com.isel.ps.gateway.service

import com.isel.ps.gateway.db.ClientRoleRepository
import com.isel.ps.gateway.model.ClientRole
import org.springframework.stereotype.Service

@Service
class ClientRoleService(private val clientRoleRepository: ClientRoleRepository) {
    fun createClientRoleAssignment(clientRole: ClientRole): ClientRole {
        return clientRoleRepository.create(clientRole)
    }
}