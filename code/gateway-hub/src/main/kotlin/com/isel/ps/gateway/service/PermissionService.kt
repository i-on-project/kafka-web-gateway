package com.isel.ps.gateway.service

import com.isel.ps.gateway.db.PermissionRepository
import com.isel.ps.gateway.model.Permission
import com.isel.ps.gateway.utils.Result
import com.isel.ps.gateway.utils.Result.Error
import com.isel.ps.gateway.utils.Result.Success
import org.springframework.stereotype.Service

sealed class PermissionCreationError {
    object InvalidTopic : PermissionCreationError()
}

typealias PermissionCreationResult = Result<PermissionCreationError, Permission>

sealed class PermissionDeletionError {
    object PermissionNotFound : PermissionDeletionError()
}

typealias PermissionDeletionResult = Result<PermissionDeletionError, Unit>

@Service
class PermissionService(private val permissionRepository: PermissionRepository) {
    fun createPermission(
        permission: Permission
    ): PermissionCreationResult {

        if (permission.topic.isBlank()) {
            Error(PermissionCreationError.InvalidTopic, "Topic is blank")
        }

        val foundPermission = permissionRepository.getPermission(permission)

        if (foundPermission != null) {
            return Success(foundPermission)
        }

        return Success(permissionRepository.create(permission))
    }

    fun deletePermission(
        permissionId: Int
    ): PermissionDeletionResult {

        if (permissionRepository.getById(permissionId) == null) {
            return Error(PermissionDeletionError.PermissionNotFound, "Permission ID \"$permissionId\" not found.")
        }

        permissionRepository.delete(permissionId)

        return Success(Unit)
    }
}