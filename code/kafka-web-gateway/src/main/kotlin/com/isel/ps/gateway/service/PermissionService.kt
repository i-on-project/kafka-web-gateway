package com.isel.ps.gateway.service

import com.isel.ps.gateway.db.PermissionRepository
import com.isel.ps.gateway.model.Permission
import org.springframework.stereotype.Service

@Service
class PermissionService(private val permissionRepository: PermissionRepository) {
    fun createClientPermission(permission: Permission): Permission {
        validateClientPermission(permission)
        permission.permissionId = permissionRepository.create(permission)
        return permission
    }

    private fun validateClientPermission(permission: Permission) {
        if (permission.topic.isBlank()) {
            throw IllegalArgumentException("Topic must not be blank.")
        }
    }
}