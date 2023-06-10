package com.isel.ps.gateway.controller

import com.isel.ps.gateway.model.ClientRole
import com.isel.ps.gateway.model.Err
import com.isel.ps.gateway.service.ClientRoleService
import com.isel.ps.gateway.utils.Result
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/client-role")
class ClientRoleController(private val clientRoleService: ClientRoleService) {

    @PostMapping
    fun createClientRole(@RequestBody clientRole: ClientRole): ResponseEntity<*> {
        return when (val createdRes = clientRoleService.createClientRole(clientRole)) {
            is Result.Success -> ResponseEntity(createdRes.value, HttpStatus.CREATED)
            is Result.Error -> ResponseEntity(Err(createdRes.reason), HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("/{clientId}/{roleId}")
    fun deleteClientRole(
        @PathVariable clientId: Long,
        @PathVariable roleId: Int
    ): ResponseEntity<*> {
        return when (val deleteRes = clientRoleService.deleteClientRole(clientId, roleId)) {
            is Result.Success -> ResponseEntity(Unit, HttpStatus.NO_CONTENT)
            is Result.Error -> ResponseEntity(Err(deleteRes.reason), HttpStatus.NOT_FOUND)
        }
    }
}
