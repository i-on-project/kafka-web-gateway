package com.isel.ps.gateway.auth

import com.isel.ps.gateway.model.Authentication

interface AuthenticationProvider {
    fun authenticate(authentication: Authentication): String?
    fun validateToken(token: String): String?
}

