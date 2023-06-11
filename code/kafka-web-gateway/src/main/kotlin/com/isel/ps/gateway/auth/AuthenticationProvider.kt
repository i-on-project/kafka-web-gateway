package com.isel.ps.gateway.auth

import com.isel.ps.gateway.model.Client

interface AuthenticationProvider {
    fun validateToken(token: String): Client?
}

