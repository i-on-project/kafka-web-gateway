package com.isel.ps.gateway.service

import com.isel.ps.gateway.db.ClientPermissionRepository
import com.isel.ps.gateway.db.ClientRepository
import com.isel.ps.gateway.db.PermissionRepository
import com.isel.ps.gateway.model.ClientPermission
import com.isel.ps.gateway.utils.Result
import org.springframework.stereotype.Service

sealed class ClientPermissionCreationError {
    object ClientNotFound : ClientPermissionCreationError()
    object PermissionNotFound : ClientPermissionCreationError()
}

typealias ClientPermissionCreationResult = Result<ClientPermissionCreationError, Unit>

sealed class ClientPermissionDeletionError {
    object ClientNotFound : ClientPermissionDeletionError()
    object PermissionNotFound : ClientPermissionDeletionError()
}

typealias ClientPermissionDeletionResult = Result<ClientPermissionDeletionError, Unit>

@Service
class ClientPermissionService(
    private val clientPermissionRepository: ClientPermissionRepository,
    private val clientRepository: ClientRepository,
    private val permissionRepository: PermissionRepository,
) {
    fun createClientPermission(
        clientPermission: ClientPermission
    ): ClientPermissionCreationResult {

        if (!clientRepository.exists(clientPermission.clientId)) {
            Result.Error(
                ClientPermissionCreationError.ClientNotFound,
                "Client ID \"${clientPermission.clientId}\" not found"
            )
        }

        if (permissionRepository.getById(clientPermission.permissionId) == null) {
            Result.Error(
                ClientPermissionCreationError.PermissionNotFound,
                "Permission ID \"${clientPermission.permissionId}\" not found"
            )
        }

        if (!clientPermissionRepository.exists(clientPermission.clientId, clientPermission.permissionId)) {
            clientPermissionRepository.create(clientPermission)
        }

        return Result.Success(Unit)
    }

    fun deleteClientPermission(
        clientId: String,
        permissionId: Int
    ): ClientPermissionDeletionResult {

        if (!clientRepository.exists(clientId)) {
            Result.Error(
                ClientPermissionDeletionError.ClientNotFound,
                "Client ID \"${clientId}\" not found"
            )
        }

        if (permissionRepository.getById(permissionId) == null) {
            Result.Error(
                ClientPermissionDeletionError.PermissionNotFound,
                "Permission ID \"${permissionId}\" not found"
            )
        }


        clientPermissionRepository.delete(clientId, permissionId)
        return Result.Success(Unit)
    }
}