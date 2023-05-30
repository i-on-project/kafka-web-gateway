package com.isel.ps.gateway.auth

interface AuthenticationProvider {
    fun validateToken(token: String): String?
}

