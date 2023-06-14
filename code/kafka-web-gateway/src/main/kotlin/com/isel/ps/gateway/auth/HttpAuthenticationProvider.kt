package com.isel.ps.gateway.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.isel.ps.gateway.model.Client
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate

class HttpAuthenticationProvider(private val authUrl: String, private val restTemplate: RestTemplate = RestTemplate()) :
    AuthenticationProvider {
    override fun validateToken(token: String): Client? {
        val headers = HttpHeaders()
        headers.setBearerAuth(token)
        val entity = HttpEntity<Void>(headers)

        val response = restTemplate.exchange(authUrl, HttpMethod.GET, entity, String::class.java)

        return if (response.statusCode == HttpStatus.OK) {
            val clientId = extractClientFromRequestBody(response.body) ?: return null
            Client(clientId)
        } else {
            null
        }
    }


    private fun extractClientFromRequestBody(requestBody: String?): String? {
        if (requestBody == null) {
            return null
        }

        return try {
            val objectMapper = ObjectMapper()
            val jsonNode = objectMapper.readTree(requestBody) // Parse request body JSON
            jsonNode["clientId"].asText() // Extract "user" parameter as string
        } catch (ex: Exception) {
            null
        }
    }
}

