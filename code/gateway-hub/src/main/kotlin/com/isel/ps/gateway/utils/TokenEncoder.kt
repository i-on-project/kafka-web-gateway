package com.isel.ps.gateway.utils

interface TokenEncoder {
    fun createTokenValidation(token: String): String
    fun validate(validationInfo: String, token: String): Boolean
}
