package com.isel.ps.gateway.controller

import com.isel.ps.gateway.model.ClientRole
import com.isel.ps.gateway.model.Permission
import com.isel.ps.gateway.model.Role
import com.isel.ps.gateway.model.RolePermission
import com.isel.ps.gateway.service.ClientRoleService
import com.isel.ps.gateway.service.PermissionService
import com.isel.ps.gateway.service.RolePermissionService
import com.isel.ps.gateway.service.RoleService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class ClientController(
    private val permissionService: PermissionService,
    private val roleService: RoleService,
    private val rolePermissionService: RolePermissionService,
    private val clientRoleService: ClientRoleService
) {

    @PostMapping("/client-permissions")
    fun createClientPermission(@RequestBody permission: Permission): ResponseEntity<Any> {
        return try {
            val createdPermission = permissionService.createClientPermission(permission)
            ResponseEntity(createdPermission, HttpStatus.CREATED)
        } catch (ex: Exception) {
            ResponseEntity("Failed to create client permission: ${ex.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/client-roles")
    fun createClientRole(@RequestBody role: Role): ResponseEntity<Any> {
        return try {
            val createdRole = roleService.createClientRole(role)
            ResponseEntity(createdRole, HttpStatus.CREATED)
        } catch (ex: Exception) {
            ResponseEntity("Failed to create client role: ${ex.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/client-role-permissions")
    fun createClientRolePermission(@RequestBody rolePermission: RolePermission): ResponseEntity<Any> {
        return try {
            val createdRolePermission = rolePermissionService.createClientRolePermission(rolePermission)
            ResponseEntity(createdRolePermission, HttpStatus.CREATED)
        } catch (ex: Exception) {
            ResponseEntity("Failed to create client role permission: ${ex.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/client-role-assignments")
    fun createClientRoleAssignment(@RequestBody clientRole: ClientRole): ResponseEntity<Any> {
        return try {
            val createdRoleAssignment = clientRoleService.createClientRoleAssignment(clientRole)
            ResponseEntity(createdRoleAssignment, HttpStatus.CREATED)
        } catch (ex: Exception) {
            ResponseEntity("Failed to create client role assignment: ${ex.message}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
