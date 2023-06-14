package com.isel.ps.demo

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

data class Client(val clientId: String)

@RestController
internal class AuthenticationController {

    @GetMapping("/authenticate")
    fun authenticate(@RequestHeader("Authorization") bearerToken: String): ResponseEntity<Client> {
        val token = bearerToken.substring(7) // Remove "Bearer " prefix

        val clientId: String = token
        return ResponseEntity.ok(Client(clientId))
    }
}