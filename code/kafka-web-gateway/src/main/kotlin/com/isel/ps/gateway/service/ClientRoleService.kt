package com.isel.ps.gateway.service

import com.isel.ps.gateway.db.ClientRepository
import com.isel.ps.gateway.db.ClientRoleRepository
import com.isel.ps.gateway.db.RoleRepository
import com.isel.ps.gateway.model.ClientRole
import com.isel.ps.gateway.utils.Result
import org.springframework.stereotype.Service

sealed class ClientRoleCreationError {
    object ClientNotFound : ClientRoleCreationError()
    object RoleNotFound : ClientRoleCreationError()
}

typealias ClientRoleCreationResult = Result<ClientRoleCreationError, Unit>

sealed class ClientRoleDeletionError {
    object ClientNotFound : ClientRoleDeletionError()
    object RoleNotFound : ClientRoleDeletionError()
}

typealias ClientRoleDeletionResult = Result<ClientRoleDeletionError, Unit>

@Service
class ClientRoleService(
    private val clientRoleRepository: ClientRoleRepository,
    private val clientRepository: ClientRepository,
    private val roleRepository: RoleRepository,
) {
    fun createClientRole(
        clientRole: ClientRole
    ): ClientRoleCreationResult {

        if (!clientRepository.exists(clientRole.clientId)) {
            Result.Error(
                ClientRoleCreationError.ClientNotFound,
                "Client ID \"${clientRole.clientId}\" not found"
            )
        }

        if (roleRepository.getById(clientRole.roleId) == null) {
            Result.Error(
                ClientRoleCreationError.RoleNotFound,
                "Role ID \"${clientRole.roleId}\" not found"
            )
        }

        if (!clientRoleRepository.exists(clientRole.clientId, clientRole.roleId)) {
            clientRoleRepository.create(clientRole)
        }

        return Result.Success(Unit)
    }

    fun deleteClientRole(
        clientId: Long,
        roleId: Int
    ): ClientRoleDeletionResult {

        if (!clientRepository.exists(clientId)) {
            Result.Error(
                ClientRoleDeletionError.ClientNotFound,
                "Client ID \"${clientId}\" not found"
            )
        }

        if (roleRepository.getById(roleId) == null) {
            Result.Error(
                ClientRoleDeletionError.RoleNotFound,
                "Role ID \"${roleId}\" not found"
            )
        }


        clientRoleRepository.delete(clientId, roleId)
        return Result.Success(Unit)
    }
}