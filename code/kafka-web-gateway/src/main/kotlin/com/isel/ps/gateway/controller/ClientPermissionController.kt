package com.isel.ps.gateway.controller

import com.isel.ps.gateway.model.ClientPermission
import com.isel.ps.gateway.model.Err
import com.isel.ps.gateway.service.ClientPermissionService
import com.isel.ps.gateway.utils.Result
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/client-permission")
class ClientPermissionController(private val clientPermissionService: ClientPermissionService) {

    @PostMapping
    fun createClientPermission(@RequestBody clientPermission: ClientPermission): ResponseEntity<*> {
        return when (val createdRes = clientPermissionService.createClientPermission(clientPermission)) {
            is Result.Success -> ResponseEntity(createdRes.value, HttpStatus.CREATED)
            is Result.Error -> ResponseEntity(Err(createdRes.reason), HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("/{clientId}/{permissionId}")
    fun deleteClientPermission(
        @PathVariable clientId: Long,
        @PathVariable permissionId: Int
    ): ResponseEntity<*> {
        return when (val deleteRes = clientPermissionService.deleteClientPermission(clientId, permissionId)) {
            is Result.Success -> ResponseEntity(Unit, HttpStatus.NO_CONTENT)
            is Result.Error -> ResponseEntity(Err(deleteRes.reason), HttpStatus.NOT_FOUND)
        }
    }
}
