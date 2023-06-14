package com.isel.ps.demo

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

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
    val name: String,
    val allowed: MutableList<String>
) {
    constructor() : this("", mutableListOf())
}

@RestController
class ChatRoomController {

    val rooms: List<Room> = mutableListOf(Room("geral", mutableListOf()), Room("privado", mutableListOf()))

    @GetMapping("/rooms")
    fun availableRooms(): ResponseEntity<List<Room>> {
        return ResponseEntity.ok(rooms)
    }

    @PostMapping("/room/{room}/{client}")
    fun givePermissionTo(@PathVariable room: String, @PathVariable client: String): Any {

        val permission = createPermission(room)

        if (permission == null) {
            println("Unable to create permission.")
            return ResponseEntity.badRequest()
        }

        return if (assignToClient(client, permission)) {
            rooms.find {
                it.name == room
            }?.allowed?.add(client)

            ResponseEntity.ok()
        } else {
            ResponseEntity.badRequest()
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