package com.isel.ps.gateway.e2e

import com.fasterxml.jackson.databind.ObjectMapper
import com.isel.ps.gateway.db.AdminRepository
import com.isel.ps.gateway.model.Admin
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.util.*

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension::class)
class AdminTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var adminRepository: AdminRepository

    @Test
    fun createAdminTest() {
        val admin = generateAdmin()

        val requestBody = ObjectMapper().writeValueAsString(admin)

        val result = mockMvc.post("/api/admin") {
            contentType = MediaType.APPLICATION_JSON
            content = requestBody
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer 348fd1e9-d401-4f4a-b4ab-917297d522c7")
        }.andExpect {
            status { isCreated() }
            content { jsonPath("$.name").value(admin.name) }
            content { jsonPath("$.description").value(admin.description) }
        }.andReturn()

        val responseJson = result.response.contentAsString
        val responseAdmin = ObjectMapper().readValue(responseJson, Admin::class.java)
        val adminId = responseAdmin.adminId

        // Assert the adminId
        assertNotNull(adminId)

        // Clean up
        adminRepository.delete(adminId!!)
    }

    @Test
    fun getAdminTest() {
        val admin = adminRepository.create(generateAdmin())

        mockMvc.get("/api/admin/{adminId}", admin.adminId) {
            contentType = MediaType.APPLICATION_JSON
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer 348fd1e9-d401-4f4a-b4ab-917297d522c7")
        }.andExpect {
            status { isOk() }
            content { jsonPath("$.adminId").value(admin.adminId) }
            content { jsonPath("$.name").value(admin.name) }
            content { jsonPath("$.description").value(admin.description) }
        }

        adminRepository.delete(admin.adminId!!)
    }

    private fun generateAdmin() = Admin(
        name = UUID.randomUUID().toString(),
        description = "Admin user",
        owner = false,
        administrative = true,
        permission = true,
        adminId = null
    )
}
