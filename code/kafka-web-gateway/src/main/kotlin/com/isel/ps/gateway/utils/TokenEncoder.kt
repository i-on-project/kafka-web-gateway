package com.isel.ps.gateway.utils

interface TokenEncoder {
    fun createValidationInformation(token: String): String
    fun validate(validationInfo: String, token: String): Boolean
}
