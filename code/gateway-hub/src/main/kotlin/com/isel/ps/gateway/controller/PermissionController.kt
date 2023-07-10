package com.isel.ps.gateway.controller

import com.isel.ps.gateway.model.Err
import com.isel.ps.gateway.model.Permission
import com.isel.ps.gateway.service.PermissionService
import com.isel.ps.gateway.utils.Result
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/permission")
class PermissionController(private val permissionService: PermissionService) {

    @PostMapping
    fun createPermission(@RequestBody permission: Permission): ResponseEntity<*> {
        return when (val createdRes = permissionService.createPermission(permission)) {
            is Result.Success -> ResponseEntity(createdRes.value, HttpStatus.CREATED)
            is Result.Error -> ResponseEntity(Err(createdRes.reason), HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("/{permissionId}")
    fun deletePermission(@PathVariable permissionId: Int): ResponseEntity<*> {
        return when (val deleteRes = permissionService.deletePermission(permissionId)) {
            is Result.Success -> ResponseEntity(Unit, HttpStatus.NO_CONTENT)
            is Result.Error -> ResponseEntity(Err(deleteRes.reason), HttpStatus.NOT_FOUND)
        }
    }
}
