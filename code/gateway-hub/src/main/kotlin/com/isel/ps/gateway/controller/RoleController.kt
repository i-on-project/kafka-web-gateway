package com.isel.ps.gateway.controller

import com.isel.ps.gateway.model.Err
import com.isel.ps.gateway.model.Role
import com.isel.ps.gateway.service.RoleService
import com.isel.ps.gateway.utils.Result
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/role")
class RoleController(private val roleService: RoleService) {

    @PostMapping
    fun createRole(@RequestBody role: Role): ResponseEntity<*> {
        return when (val createdRes = roleService.createRole(role)) {
            is Result.Success -> ResponseEntity(createdRes.value, HttpStatus.CREATED)
            is Result.Error -> ResponseEntity(Err(createdRes.reason), HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("/{roleId}")
    fun deleteRole(@PathVariable roleId: Int): ResponseEntity<*> {
        return when (val deleteRes = roleService.deleteRole(roleId)) {
            is Result.Success -> ResponseEntity(Unit, HttpStatus.NO_CONTENT)
            is Result.Error -> ResponseEntity(Err(deleteRes.reason), HttpStatus.NOT_FOUND)
        }
    }
}