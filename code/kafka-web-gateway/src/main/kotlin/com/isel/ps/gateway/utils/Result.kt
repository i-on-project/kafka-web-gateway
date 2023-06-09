package com.isel.ps.gateway.utils

sealed class Result<out Err, out Sux> {
    data class Error<out Err>(val value: Err, val reason: String) : Result<Err, Nothing>()
    data class Success<out Sux>(val value: Sux) : Result<Nothing, Sux>()
}
