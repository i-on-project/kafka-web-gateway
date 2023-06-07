package com.isel.ps.gateway.service

import com.isel.ps.gateway.db.RolePermissionRepository
import com.isel.ps.gateway.model.RolePermission
import org.springframework.stereotype.Service

@Service
class RolePermissionService(private val rolePermissionRepository: RolePermissionRepository) {
    fun createClientRolePermission(rolePermission: RolePermission): RolePermission {
        return rolePermissionRepository.create(rolePermission)
    }
}