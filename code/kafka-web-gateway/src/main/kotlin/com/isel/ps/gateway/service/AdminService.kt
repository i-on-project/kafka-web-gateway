package com.isel.ps.gateway.service


import com.isel.ps.gateway.db.AdminRepository
import com.isel.ps.gateway.db.AdminTokenRepository
import com.isel.ps.gateway.model.Admin
import com.isel.ps.gateway.utils.AdminLogic
import com.isel.ps.gateway.utils.TokenEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AdminService(
    private val adminRepository: AdminRepository,
    private val adminTokenRepository: AdminTokenRepository,
    private val adminLogic: AdminLogic,
    private val passwordEncoder: PasswordEncoder,
    private val tokenEncoder: TokenEncoder
) {
    fun createAdmin(
        name: String,
        description: String?,
        owner: Boolean = false,
        administrative: Boolean = false,
        permission: Boolean = false
    ): Admin {
        var admin = Admin(
            name = name,
            description = description,
            owner = owner,
            adminId = null,
            administrative = administrative,
            permission = permission
        )

        admin = adminRepository.create(admin)
        return admin
    }

    fun getByName(name: String): Admin? {
        return adminRepository.getByName(name)
    }
}