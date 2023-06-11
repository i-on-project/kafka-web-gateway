package com.isel.ps.gateway.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.isel.ps.gateway.model.Client
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate

class HttpAuthenticationProvider(private val authUrl: String, private val restTemplate: RestTemplate = RestTemplate()) :
    AuthenticationProvider {
    override fun validateToken(token: String): Client? {
        val headers = HttpHeaders()
        headers.setBearerAuth(token)

        val response = restTemplate.getForEntity(authUrl, Void::class.java)

        return if (response.statusCode === HttpStatus.OK) {
            val clientId = extractClientFromRequestBody(response.body as String?) ?: return null
            Client(clientId)
        } else {
            null
        }
    }

    private fun extractClientFromRequestBody(requestBody: String?): Long? {
        if (requestBody == null) {
            return null
        }

        return try {
            val objectMapper = ObjectMapper()
            val jsonNode = objectMapper.readTree(requestBody) // Parse request body JSON
            jsonNode["clientId"].asLong() // Extract "user" parameter as string
        } catch (ex: Exception) {
            null
        }
    }
}

