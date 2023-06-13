package com.isel.ps.gateway.controller

import com.isel.ps.gateway.model.Err
import com.isel.ps.gateway.model.RolePermission
import com.isel.ps.gateway.service.RolePermissionService
import com.isel.ps.gateway.utils.Result
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/role-permission")
class RolePermissionController(private val rolePermissionService: RolePermissionService) {

    @PostMapping
    fun createRolePermission(@RequestBody rolePermission: RolePermission): ResponseEntity<*> {
        return when (val createdRes = rolePermissionService.createRolePermission(rolePermission)) {
            is Result.Success -> ResponseEntity(createdRes.value, HttpStatus.CREATED)
            is Result.Error -> ResponseEntity(Err(createdRes.reason), HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("/{roleId}/{permissionId}")
    fun deleteRolePermission(
        @PathVariable roleId: Int,
        @PathVariable permissionId: Int
    ): ResponseEntity<*> {
        return when (val deleteRes = rolePermissionService.deleteRolePermission(roleId, permissionId)) {
            is Result.Success -> ResponseEntity(Unit, HttpStatus.NO_CONTENT)
            is Result.Error -> ResponseEntity(Err(deleteRes.reason), HttpStatus.NOT_FOUND)
        }
    }
}
