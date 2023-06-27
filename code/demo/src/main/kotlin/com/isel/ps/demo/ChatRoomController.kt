package com.isel.ps.demo

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import java.util.*

data class Permission(
    var permissionId: Int?,
    val topic: String,
    val key: String?,
    val read: Boolean,
    val write: Boolean
) {
    constructor() : this(null, "", null, false, false)
}

data class ClientPermission(
    val clientId: String,
    val permissionId: Int
) {
    constructor() : this("", 0)
}

data class Room(
    val id: String,
    val title: String,
    val allowed: MutableList<String>
) {
    constructor() : this("", "", mutableListOf())
}

@RestController
class ChatRoomController {

    val rooms: List<Room> = mutableListOf(
        Room("8d818415-7f23-45f3-b909-541aae83a15f", "geral", mutableListOf()),
        Room("154e0098-ebe4-4dd7-bce2-f2168eb3972e", "privado", mutableListOf())
    )

    @GetMapping("/rooms")
    fun availableRooms(): ResponseEntity<List<Room>> {
        return ResponseEntity.ok(rooms)
    }

    @GetMapping("/room/{room}/{client}")
    fun givePermissionTo(@PathVariable room: String, @PathVariable client: String): ResponseEntity<*> {

        val permission = createPermission(room)

        if (permission == null) {
            println("Unable to create permission.")
            return ResponseEntity.badRequest().body(Unit)

        }

        return if (assignToClient(client, permission)) {
            rooms.find {
                it.title == room
            }?.allowed?.add(client)

            ResponseEntity.ok(Unit)
        } else {
            ResponseEntity.badRequest().body(Unit)
        }
    }

    private fun createPermission(name: String): Permission? {
        val restTemplate = RestTemplate()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth("348fd1e9-d401-4f4a-b4ab-917297d522c7")

        val requestBody = Permission(null, name, null, true, true)
        val httpEntity = HttpEntity(requestBody, headers)

        val url = "http://localhost:8080/api/permission"

        val response = restTemplate.exchange(
            url, HttpMethod.POST, httpEntity,
            String::class.java
        )

        val permission: Permission?

        if (response.statusCode == HttpStatus.CREATED) {
            val responseBody = response.body

            val objectMapper = ObjectMapper()
            try {
                permission = objectMapper.readValue(responseBody, Permission::class.java)
                println("ID: " + permission.permissionId)
                println("Topic: " + permission.topic)
            } catch (e: Exception) {
                System.err.println("Failed to parse response body: " + e.message)
                return null
            }
        } else {
            System.err.println("Request failed with status: " + response.statusCode)
            return null
        }

        return permission
    }

    private fun assignToClient(client: String, permission: Permission): Boolean {
        val restTemplate = RestTemplate()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth("348fd1e9-d401-4f4a-b4ab-917297d522c7")

        val requestBody = ClientPermission(client, permission.permissionId!!)
        val httpEntity = HttpEntity(requestBody, headers)

        val url = "http://localhost:8080/api/client-permission"

        val response = restTemplate.exchange(
            url, HttpMethod.POST, httpEntity,
            String::class.java
        )


        return if (response.statusCode == HttpStatus.CREATED) {
            true
        } else {
            System.err.println("Request failed with status: " + response.statusCode)
            false
        }
    }
}