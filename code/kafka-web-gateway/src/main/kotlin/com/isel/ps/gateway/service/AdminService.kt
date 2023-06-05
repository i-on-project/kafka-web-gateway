package com.isel.ps.gateway.service


import com.isel.ps.gateway.db.AdminRepository
import com.isel.ps.gateway.db.AdminTokenRepository
import com.isel.ps.gateway.model.GatewayEntities.Companion.Admin
import com.isel.ps.gateway.model.GatewayEntities.Companion.AdminToken
import com.isel.ps.gateway.utils.TokenEncoder
import com.isel.ps.gateway.utils.AdminLogic
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant

@Service
class AdminService(
    private val adminRepository: AdminRepository,
    private val adminTokenRepository: AdminTokenRepository,
    private val adminLogic: AdminLogic,
    private val passwordEncoder: PasswordEncoder,
    private val tokenEncoder: TokenEncoder
) {
    fun createAdmin(username: String, password: String, description: String?, owner: Boolean): Admin {

        if (username.length < 4 || username.length > 36) {
            throw Exception("Username too short")
        }

        if (!adminLogic.isSafePassword(password)) {
            throw Exception("Password not safe")
        }

        val passwordValidation = passwordEncoder.encode(password)

        var admin = Admin(
            username = username,
            passwordValidation = passwordValidation,
            description = description,
            owner = owner,
            adminId = null
        )

        admin = adminRepository.create(admin)
        return admin
    }

    fun createToken(username: String, password: String, description: String?, owner: Boolean): AdminToken {

        if (username.isBlank() || password.isBlank()) {
            throw Exception("Invalid user or pwd")
        }

        val admin: Admin = adminRepository.getByUsername(username) ?: throw Exception("Admin doesnt exist")

        if (!passwordEncoder.matches(password, admin.passwordValidation)) {
            throw Exception("Invalid PWD")
        }

        val generatedToken = adminLogic.generateToken()
        val now = Timestamp.from(Instant.now())

        val adminToken: AdminToken =
            AdminToken(
                tokenValidation = tokenEncoder.createValidationInformation(generatedToken),
                adminId = admin.adminId!!,
                now,
                now
            )
        adminTokenRepository.create(adminToken)
        return adminToken
    }

    fun getByUsername(username: String): Admin? {
        return adminRepository.getByUsername(username)
    }


    fun getUserByToken(token: String): UserEntity? {
        if (!userLogic.canBeToken(token)) {
            return null
        }

        val tokenValidationInfo = tokenEncoder.createValidationInformation(token)
        val tokenEntity = usersRepository.findTokenByTokenValidationInfo(tokenValidationInfo)

        return if (tokenEntity != null && isTokenStillValid(tokenEntity)) {
            usersRepository.updateTokenLastUsed(tokenEntity, clock.now())
            tokenEntity.user
        } else {
            null
        }
    }

     */

    private fun isTokenStillValid(token: AdminToken): Boolean {
        return Instant.now().isBefore(Instant.ofEpochSecond(token.createdAt.time).plus(TOKEN_TTL))
    }

    companion object {
        val TOKEN_ROLLING_TTL: Duration = Duration.ofHours(1)
        val TOKEN_TTL: Duration = Duration.ofDays(1)
        const val MAX_TOKENS: Int = 3
    }
}