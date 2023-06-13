package com.isel.ps.gateway.service


import com.isel.ps.gateway.db.RoleRepository
import com.isel.ps.gateway.model.Role
import com.isel.ps.gateway.utils.Result
import com.isel.ps.gateway.utils.Result.Error
import com.isel.ps.gateway.utils.Result.Success
import org.springframework.stereotype.Service

sealed class RoleCreationError {
    object InvalidName : RoleCreationError()
    object RoleAlreadyExists : RoleCreationError()

}

typealias RoleCreationResult = Result<RoleCreationError, Role>

sealed class RoleDeletionError {
    object RoleNotFound : RoleDeletionError()
}

typealias RoleDeletionResult = Result<RoleDeletionError, Unit>

@Service
class RoleService(private val roleRepository: RoleRepository) {
    fun createRole(
        role: Role
    ): RoleCreationResult {

        if (role.name.isBlank()) {
            Error(RoleCreationError.InvalidName, "Name is blank")
        }

        if (roleRepository.getByName(role.name) != null) {
            return Error(RoleCreationError.RoleAlreadyExists, "Role \"${role.name}\" already exists.")
        }

        val createdRole = roleRepository.create(role)
        return Success(createdRole)
    }

    fun deleteRole(
        roleId: Int
    ): RoleDeletionResult {

        if (roleRepository.getById(roleId) == null) {
            return Error(RoleDeletionError.RoleNotFound, "role ID \"$roleId\" not found.")
        }

        roleRepository.delete(roleId)

        return Success(Unit)
    }
}