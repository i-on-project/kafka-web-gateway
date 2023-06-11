package com.isel.ps.gateway.auth

import com.isel.ps.gateway.model.AdminToken
import com.isel.ps.gateway.service.AdminService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class HandlerAuthenticationInterceptor(val adminService: AdminService) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val authHeader = request.getHeader("Authorization")
        val adminToken = processAdminToken(authHeader) ?: return false

        request.setAttribute("adminId", adminToken.adminId)
        return true
    }

    private fun processAdminToken(authorizationValue: String?): AdminToken? {
        if (authorizationValue == null) {
            return null
        }
        val parts = authorizationValue.trim().split(" ")
        if (parts.size != 2) {
            return null
        }
        if (parts[0].lowercase() != "bearer") {
            return null
        }
        return adminService.getByToken(parts[1])
    }
}
