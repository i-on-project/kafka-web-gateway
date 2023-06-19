package com.isel.ps.gateway.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.isel.ps.gateway.model.Client
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL


class HttpAuthenticationProvider(private val authUrl: String, private val restTemplate: RestTemplate = RestTemplate()) :
    AuthenticationProvider {
    private val logger: Logger = LoggerFactory.getLogger(HttpAuthenticationProvider::class.java)

    override fun validateToken(token: String): Client? {
        if (!isValidURL(authUrl)) {
            logger.warn("Authentication URL is not valid.")
            return null
        }

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

    private fun isValidURL(url: String?): Boolean {
        return try {
            URL(url).toURI()
            true
        } catch (e: MalformedURLException) {
            false
        } catch (e: URISyntaxException) {
            false
        }
    }
}

