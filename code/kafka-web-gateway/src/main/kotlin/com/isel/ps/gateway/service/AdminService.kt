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
    object AdminAlreadyExists : AdminCreationError()
    object OwnerAlreadyExists : AdminCreationError()

    object InvalidNameLength : AdminCreationError()
}

typealias AdminCreationResult = Result<AdminCreationError, Admin>

sealed class TokenCreationError {
    object AdminTokenAlreadyExists : TokenCreationError()
    object AdminNotFound : TokenCreationError()
    object InvalidTokenLength : TokenCreationError()
}

typealias TokenCreationResult = Result<TokenCreationError, AdminToken>

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
        permission: Boolean = false
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
            owner = owner,
            adminId = null,
            administrative = administrative,
            permission = permission
        )

        admin = adminRepository.create(admin)

        return Result.Success(admin)
    }

    fun createToken(
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

    fun getByName(name: String): Admin? {
        if (name.length < 3 || name.length > 64) {
            return null
        }

        return adminRepository.getByName(name)
    }
}