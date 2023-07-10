package com.isel.ps.gateway.service

import com.isel.ps.gateway.db.PermissionRepository
import com.isel.ps.gateway.db.RolePermissionRepository
import com.isel.ps.gateway.db.RoleRepository
import com.isel.ps.gateway.model.RolePermission
import com.isel.ps.gateway.utils.Result
import org.springframework.stereotype.Service

sealed class RolePermissionCreationError {
    object RoleNotFound : RolePermissionCreationError()
    object PermissionNotFound : RolePermissionCreationError()
}

typealias RolePermissionCreationResult = Result<RolePermissionCreationError, Unit>

sealed class RolePermissionDeletionError {
    object RoleNotFound : RolePermissionDeletionError()
    object PermissionNotFound : RolePermissionDeletionError()
}

typealias RolePermissionDeletionResult = Result<RolePermissionDeletionError, Unit>

@Service
class RolePermissionService(
    private val rolePermissionRepository: RolePermissionRepository,
    private val roleRepository: RoleRepository,
    private val permissionRepository: PermissionRepository,
) {
    fun createRolePermission(
        rolePermission: RolePermission
    ): RolePermissionCreationResult {

        if (roleRepository.getById(rolePermission.roleId) == null) {
            Result.Error(
                RolePermissionCreationError.RoleNotFound,
                "Role ID \"${rolePermission.roleId}\" not found"
            )
        }

        if (permissionRepository.getById(rolePermission.permissionId) == null) {
            Result.Error(
                RolePermissionCreationError.PermissionNotFound,
                "Permission ID \"${rolePermission.permissionId}\" not found"
            )
        }

        if (!rolePermissionRepository.exists(rolePermission.roleId, rolePermission.permissionId)) {
            rolePermissionRepository.create(rolePermission)
        }

        return Result.Success(Unit)
    }

    fun deleteRolePermission(
        roleId: Int,
        permissionId: Int
    ): RolePermissionDeletionResult {

        if (roleRepository.getById(roleId) == null) {
            Result.Error(
                RolePermissionDeletionError.RoleNotFound,
                "Role ID \"${roleId}\" not found"
            )
        }

        if (permissionRepository.getById(permissionId) == null) {
            Result.Error(
                RolePermissionDeletionError.PermissionNotFound,
                "Permission ID \"${permissionId}\" not found"
            )
        }


        rolePermissionRepository.delete(roleId, permissionId)
        return Result.Success(Unit)
    }
}