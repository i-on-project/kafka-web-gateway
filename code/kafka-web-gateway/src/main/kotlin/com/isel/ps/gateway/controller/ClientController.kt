package com.isel.ps.gateway.controller

import com.isel.ps.gateway.model.GatewayEntities.Companion.ClientPermission
import com.isel.ps.gateway.model.GatewayEntities.Companion.ClientRole
import com.isel.ps.gateway.model.GatewayEntities.Companion.ClientRoleAssignment
import com.isel.ps.gateway.model.GatewayEntities.Companion.ClientRolePermission
import com.isel.ps.gateway.service.ClientPermissionService
import com.isel.ps.gateway.service.ClientRoleAssignmentService
import com.isel.ps.gateway.service.ClientRolePermissionService
import com.isel.ps.gateway.service.ClientRoleService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class ClientController(
    private val clientPermissionService: ClientPermissionService,
    private val clientRoleService: ClientRoleService,
    private val clientRolePermissionService: ClientRolePermissionService,
    private val clientRoleAssignmentService: ClientRoleAssignmentService
) {

    @PostMapping("/client-permissions")
    fun createClientPermission(@RequestBody clientPermission: ClientPermission): ResponseEntity<Any> {
        return try {
            val createdPermission = clientPermissionService.createClientPermission(clientPermission)
            ResponseEntity(createdPermission, HttpStatus.CREATED)
        } catch (ex: Exception) {
            ResponseEntity("Failed to create client permission: ${ex.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/client-roles")
    fun createClientRole(@RequestBody clientRole: ClientRole): ResponseEntity<Any> {
        return try {
            val createdRole = clientRoleService.createClientRole(clientRole)
            ResponseEntity(createdRole, HttpStatus.CREATED)
        } catch (ex: Exception) {
            ResponseEntity("Failed to create client role: ${ex.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/client-role-permissions")
    fun createClientRolePermission(@RequestBody clientRolePermission: ClientRolePermission): ResponseEntity<Any> {
        return try {
            val createdRolePermission = clientRolePermissionService.createClientRolePermission(clientRolePermission)
            ResponseEntity(createdRolePermission, HttpStatus.CREATED)
        } catch (ex: Exception) {
            ResponseEntity("Failed to create client role permission: ${ex.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/client-role-assignments")
    fun createClientRoleAssignment(@RequestBody clientRoleAssignment: ClientRoleAssignment): ResponseEntity<Any> {
        return try {
            val createdRoleAssignment = clientRoleAssignmentService.createClientRoleAssignment(clientRoleAssignment)
            ResponseEntity(createdRoleAssignment, HttpStatus.CREATED)
        } catch (ex: Exception) {
            ResponseEntity("Failed to create client role assignment: ${ex.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
