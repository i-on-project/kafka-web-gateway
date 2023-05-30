package com.isel.ps.gateway.service

import com.isel.ps.gateway.db.ClientRoleAssignmentRepository
import com.isel.ps.gateway.model.GatewayEntities.Companion.ClientRoleAssignment
import org.springframework.stereotype.Service

@Service
class ClientRoleAssignmentService(private val clientRoleAssignmentRepository: ClientRoleAssignmentRepository) {
    fun createClientRoleAssignment(clientRoleAssignment: ClientRoleAssignment): ClientRoleAssignment {
        return clientRoleAssignmentRepository.create(clientRoleAssignment)
    }
}