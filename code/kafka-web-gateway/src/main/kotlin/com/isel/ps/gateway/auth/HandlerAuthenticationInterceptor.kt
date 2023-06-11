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
        val authHeader = request.getHeader(NAME_AUTHORIZATION_HEADER)
        val adminToken = processAdminToken(authHeader)
        if (adminToken == null) {
            response.status = 401
            response.addHeader(NAME_WWW_AUTHENTICATE_HEADER, BEARER_SCHEME)
            return false
        }

        request.setAttribute(ADMIN_ID_ATTRIBUTE, adminToken.adminId)
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
        if (parts[0].lowercase() != BEARER_SCHEME) {
            return null
        }
        return adminService.getByToken(parts[1])
    }

    companion object {
        private const val NAME_AUTHORIZATION_HEADER = "Authorization"
        private const val NAME_WWW_AUTHENTICATE_HEADER = "WWW-Authenticate"
        private const val BEARER_SCHEME = "bearer"
        const val ADMIN_ID_ATTRIBUTE = "adminId"
    }
}
