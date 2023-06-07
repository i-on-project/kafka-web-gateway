package com.isel.ps.gateway.service

import com.isel.ps.gateway.db.RoleRepository
import com.isel.ps.gateway.model.Role
import org.springframework.stereotype.Service

@Service
class RoleService(private val roleRepository: RoleRepository) {
    fun createClientRole(role: Role): Role {
        validateClientRole(role)
        return roleRepository.create(role)
    }

    private fun validateClientRole(role: Role) {
        if (role.name.isBlank()) {
            throw IllegalArgumentException("Role must not be blank.")
        }
    }
}