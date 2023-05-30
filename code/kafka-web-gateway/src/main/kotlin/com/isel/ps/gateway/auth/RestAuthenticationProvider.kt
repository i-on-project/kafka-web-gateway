package com.isel.ps.gateway.auth

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate

class RestAuthenticationProvider(private val authUrl: String, private val restTemplate: RestTemplate = RestTemplate()) :
    AuthenticationProvider {
    override fun validateToken(token: String): String? {
        val headers = HttpHeaders()
        headers.setBearerAuth(token)

        val response = restTemplate.getForEntity(authUrl, Void::class.java)

        return if (response.statusCode === HttpStatus.OK) {
            extractUserFromRequestBody(response.body as String)
        } else {
            return null
        }
    }

    private fun extractUserFromRequestBody(requestBody: String): String {
        val objectMapper = ObjectMapper()
        val jsonNode = objectMapper.readTree(requestBody) // Parse request body JSON
        return jsonNode["user"].asText() // Extract "user" parameter as string
    }
}

