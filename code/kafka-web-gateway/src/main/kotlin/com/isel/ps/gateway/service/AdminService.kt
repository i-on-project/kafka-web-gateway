package com.isel.ps.gateway.service

import com.isel.ps.gateway.db.AdminRepository
import com.isel.ps.gateway.db.AdminTokenRepository
import com.isel.ps.gateway.model.Admin
import com.isel.ps.gateway.model.AdminToken
import com.isel.ps.gateway.utils.Result
import com.isel.ps.gateway.utils.Result.Error
import com.isel.ps.gateway.utils.TokenEncoder
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant

sealed class AdminCreationError {
    object NoAdministrativePrivileges : AdminCreationError()
    object AdminAlreadyExists : AdminCreationError()
    object OwnerAlreadyExists : AdminCreationError()
    object InvalidNameLength : AdminCreationError()
    object AdminNotFound : AdminCreationError()
}

typealias AdminCreationResult = Result<AdminCreationError, Admin>

sealed class AdminUpdateError {
    object NoAdministrativePrivileges : AdminUpdateError()
    object CannotChangeOwner : AdminUpdateError()
    object CannotChangeAdministrative : AdminUpdateError()
    object CannotChangePermission : AdminUpdateError()
    object AdminNotFound : AdminUpdateError()
}

typealias AdminUpdateResult = Result<AdminUpdateError, Admin>

sealed class AdminDeletionError {
    object AdminNotFound : AdminDeletionError()
    object AdminSelfDelete : AdminDeletionError()
    object NoAdministrativePrivileges : AdminDeletionError()
    object AdminIsOwner : AdminDeletionError()
}

typealias AdminDeletionResult = Result<AdminDeletionError, Unit>

sealed class TokenCreationError {
    object AdminTokenAlreadyExists : TokenCreationError()
    object AdminNotFound : TokenCreationError()
    object InvalidTokenLength : TokenCreationError()
    object NoAdministrativePrivileges : TokenCreationError()
}

typealias TokenCreationResult = Result<TokenCreationError, AdminToken>

sealed class TokenDeletionError {
    object AdminNotFound : TokenDeletionError()
    object NoAdministrativePrivileges : TokenDeletionError()
    object TokenNotFound : TokenDeletionError()

}

typealias TokenDeletionResult = Result<TokenDeletionError, Unit>

@Service
class AdminService(
    private val adminRepository: AdminRepository,
    private val adminTokenRepository: AdminTokenRepository,
    private val tokenEncoder: TokenEncoder
) {
    fun createAdmin(
        name: String,
        description: String?,
        owner: Boolean = false,
        administrative: Boolean = false,
        permission: Boolean = false,
        currAdminId: Int
    ): AdminCreationResult {

        if (name.length < 3 || name.length > 64) {
            return Error(AdminCreationError.InvalidNameLength, "Admin name \"$name\" must be between 4 and 64 digits")
        }

        if (adminRepository.getByName(name) != null) {
            return Error(AdminCreationError.AdminAlreadyExists, "Admin \"$name\" already exists.")
        }

        val currAdmin = adminRepository.getById(currAdminId)
            ?: return Error(
                AdminCreationError.AdminNotFound,
                "Admin \"$currAdminId\" not found."
            )

        if (!(currAdmin.owner || currAdmin.administrative)) {
            return Error(
                AdminCreationError.NoAdministrativePrivileges,
                "Admin \"$currAdminId\" has no administrative privileges"
            )
        }

        if (adminRepository.ownerExists()) {
            return Error(AdminCreationError.OwnerAlreadyExists, "An owner already exists.")
        }

        var admin = Admin(
            name = name,
            description = description,
            owner = owner,
            adminId = null,
            administrative = administrative,
            permission = permission
        )

        admin = adminRepository.create(admin)

        return Result.Success(admin)
    }

    fun createOwner(
        name: String,
        description: String?
    ): AdminCreationResult {

        if (name.length < 3 || name.length > 64) {
            return Error(AdminCreationError.InvalidNameLength, "Admin name \"$name\" must be between 4 and 64 digits")
        }

        if (adminRepository.getByName(name) != null) {
            return Error(AdminCreationError.AdminAlreadyExists, "Admin \"$name\" already exists.")
        }

        if (adminRepository.ownerExists()) {
            return Error(AdminCreationError.OwnerAlreadyExists, "An owner already exists.")
        }

        var admin = Admin(
            name = name,
            description = description,
            owner = true,
            adminId = null,
            administrative = true,
            permission = true
        )

        admin = adminRepository.create(admin)

        return Result.Success(admin)
    }

    fun createToken(
        token: String,
        adminId: Int,
        currAdminId: Int
    ): Result<TokenCreationError, AdminToken> {
        if (token.length < 16 || token.length > 255) {
            return Error(
                TokenCreationError.InvalidTokenLength,
                "Admin token must be between 16 and 255 characters"
            )
        }

        val admin = adminRepository.getById(adminId)
            ?: return Error(TokenCreationError.AdminNotFound, "Admin \"$adminId\" not found.")

        val currAdmin = adminRepository.getById(currAdminId)
            ?: return Error(TokenCreationError.AdminNotFound, "Current admin \"$currAdminId\" not found.")

        if (!(currAdmin.owner || currAdmin.administrative)) {
            return Error(
                TokenCreationError.NoAdministrativePrivileges,
                "Current admin \"$currAdminId\" has no administrative privileges."
            )
        }

        val tokenValidation = tokenEncoder.createTokenValidation(token)

        if (adminTokenRepository.getByTokenValidation(tokenValidation) != null) {
            return Error(TokenCreationError.AdminTokenAlreadyExists, "Admin token already exists.")
        }

        val adminToken = AdminToken(tokenValidation, adminId, null, Timestamp.from(Instant.now()))
        adminTokenRepository.create(adminToken)

        return Result.Success(adminToken)
    }

    fun createOwnerToken(
        token: String,
        adminId: Int
    ): TokenCreationResult {

        if (token.length < 16 || token.length > 255) {
            return Error(
                TokenCreationError.InvalidTokenLength,
                "Admin token must be between 16 and 255 digits"
            )
        }

        if (adminRepository.getById(adminId) == null) {
            return Error(TokenCreationError.AdminNotFound, "Admin \"$adminId\" not found")
        }

        val tokenValidation = tokenEncoder.createTokenValidation(token)

        if (adminTokenRepository.getByTokenValidation(tokenValidation) != null) {
            return Error(TokenCreationError.AdminTokenAlreadyExists, "Admin token already exists")
        }

        val adminToken = AdminToken(tokenValidation, adminId, null, Timestamp.from(Instant.now()))

        adminTokenRepository.create(adminToken)

        return Result.Success(adminToken)
    }

    fun updateAdmin(
        adminId: Int,
        name: String,
        description: String?,
        owner: Boolean,
        administrative: Boolean,
        permission: Boolean,
        currAdminId: Int
    ): AdminUpdateResult {
        val admin = adminRepository.getById(adminId)
            ?: return Error(AdminUpdateError.AdminNotFound, "Admin \"$adminId\" not found.")

        if (admin.owner && !owner) {
            return Error(AdminUpdateError.CannotChangeOwner, "Cannot change owner status for admin \"$adminId\".")
        }

        if (admin.administrative && !administrative) {
            return Error(
                AdminUpdateError.CannotChangeAdministrative,
                "Cannot change administrative status for admin \"$adminId\"."
            )
        }

        if (admin.permission && !permission) {
            return Error(
                AdminUpdateError.CannotChangePermission,
                "Cannot change permission status for admin \"$adminId\"."
            )
        }

        val currAdmin = adminRepository.getById(currAdminId)
            ?: return Error(AdminUpdateError.AdminNotFound, "Current admin \"$currAdminId\" not found.")

        if (!currAdmin.owner && !currAdmin.administrative) {
            return Error(
                AdminUpdateError.NoAdministrativePrivileges,
                "Current admin \"$currAdminId\" has no administrative privileges."
            )
        }

        val updatedAdmin = admin.copy(
            name = name,
            description = description,
            owner = owner,
            administrative = administrative,
            permission = permission
        )

        adminRepository.update(updatedAdmin)

        return Result.Success(updatedAdmin)
    }

    fun deleteAdmin(
        adminId: Int,
        currAdminId: Int
    ): AdminDeletionResult {

        if (adminId == currAdminId) {
            return Error(AdminDeletionError.AdminSelfDelete, "Admin \"$adminId\" cannot self delete.")
        }

        val admin = adminRepository.getById(adminId)
            ?: return Error(AdminDeletionError.AdminNotFound, "Admin \"$currAdminId\" not found.")

        val currAdmin = adminRepository.getById(currAdminId)
            ?: return Error(AdminDeletionError.AdminNotFound, "Current admin \"$currAdminId\" not found.")

        if (currAdmin.owner) {
            adminRepository.delete(adminId)
        } else {
            if (!currAdmin.administrative) {
                return Error(
                    AdminDeletionError.NoAdministrativePrivileges,
                    "Current admin \"$currAdminId\" has no administrative privileges."
                )
            }

            if (admin.owner) {
                return Error(AdminDeletionError.AdminIsOwner, "Admin \"$adminId\" is owner")
            }

            adminRepository.delete(adminId)
        }

        return Result.Success(Unit)
    }

    fun deleteToken(
        tokenValidation: String,
        adminId: Int,
        currAdminId: Int
    ): TokenDeletionResult {
        val adminToken = adminTokenRepository.getByTokenValidation(tokenValidation)
            ?: return Error(TokenDeletionError.TokenNotFound, "Token \"$tokenValidation\" not found.")

        val admin = adminRepository.getById(adminId)
            ?: return Error(TokenDeletionError.AdminNotFound, "Admin \"$adminId\" not found.")

        val currAdmin = adminRepository.getById(currAdminId)
            ?: return Error(TokenDeletionError.AdminNotFound, "Current admin \"$currAdminId\" not found.")

        if (!(currAdmin.owner || currAdmin.administrative)) {
            return Error(
                TokenDeletionError.NoAdministrativePrivileges,
                "Current admin \"$currAdminId\" has no administrative privileges."
            )
        }

        adminTokenRepository.delete(tokenValidation)

        return Result.Success(Unit)
    }

    fun getByName(name: String): Admin? {
        if (name.length < 3 || name.length > 64) {
            return null
        }

        return adminRepository.getByName(name)
    }

    fun getById(adminId: Int): Admin? {
        return adminRepository.getById(adminId)
    }

    fun getByToken(token: String): AdminToken? {
        if (token.length < 16 || token.length > 255) {
            return null
        }

        val tokenValidation = tokenEncoder.createTokenValidation(token)

        return adminTokenRepository.getByTokenValidation(tokenValidation)
    }
}