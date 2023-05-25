package com.isel.ps.gateway.auth

import com.isel.ps.gateway.model.Authentication
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate

class RestAuthenticationProvider(private val authUrl: String, private val restTemplate: RestTemplate = RestTemplate()) :
    AuthenticationProvider {

    override fun authenticate(authentication: Authentication): String? {
        // TODO:
        return null
    }

    override fun validateToken(token: String): String? {
        val headers = HttpHeaders()
        headers.setBearerAuth(token);

        val response = restTemplate.getForEntity(authUrl, Void::class.java)

        return if (response.statusCode === HttpStatus.OK) {
            response.headers["User"]?.first()
        } else {
            return null
        }
    }
}

