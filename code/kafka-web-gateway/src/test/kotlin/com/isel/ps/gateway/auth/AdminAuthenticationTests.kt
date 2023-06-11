package com.isel.ps.gateway.auth

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension::class)
class AdminAuthenticationTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `Request with valid authentication header should return 200 OK`() {
        val token = "348fd1e9-d401-4f4a-b4ab-917297d522c7"

        mockMvc.get("/api/admin/1") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `Request without authentication header should return 401 Unauthorized`() {
        mockMvc.get("/api/admin/1") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isUnauthorized() }
        }
    }
}