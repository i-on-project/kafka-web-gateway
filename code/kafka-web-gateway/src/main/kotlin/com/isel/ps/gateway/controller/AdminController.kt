package com.isel.ps.gateway.controller

import com.isel.ps.gateway.auth.HandlerAuthenticationInterceptor.Companion.ADMIN_ID_ATTRIBUTE
import com.isel.ps.gateway.model.Admin
import com.isel.ps.gateway.model.Err
import com.isel.ps.gateway.service.AdminService
import com.isel.ps.gateway.utils.Result
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin")
class AdminController(private val adminService: AdminService) {

    @GetMapping("/{adminId}")
    fun getAdmin(@PathVariable adminId: Int): ResponseEntity<*> {
        return when (val createdRes = adminService.getAdmin(adminId)) {
            is Result.Success -> ResponseEntity(createdRes.value, HttpStatus.OK)
            is Result.Error -> ResponseEntity(Unit, HttpStatus.NOT_FOUND)
        }
    }

    @PostMapping
    fun createAdmin(@RequestBody admin: Admin, request: HttpServletRequest): ResponseEntity<*> {
        val currAdmin =
            extractAdminFromRequest(request) ?: return ResponseEntity(Err("Admin not found."), HttpStatus.UNAUTHORIZED)

        return when (val createdRes = adminService.createAdmin(
            admin.name,
            admin.description,
            admin.owner,
            admin.administrative,
            admin.permission,
            currAdmin.adminId!!
        )) {
            is Result.Success -> ResponseEntity(createdRes.value, HttpStatus.CREATED)
            is Result.Error -> ResponseEntity(Err(createdRes.reason), HttpStatus.BAD_REQUEST)
        }
    }

    @PutMapping("/{adminId}")
    fun updateAdmin(
        @PathVariable adminId: Int,
        @RequestBody admin: Admin,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        val currAdmin =
            extractAdminFromRequest(request) ?: return ResponseEntity(Err("Admin not found"), HttpStatus.UNAUTHORIZED)

        return when (val updateRes = adminService.updateAdmin(
            adminId,
            admin.name,
            admin.description,
            admin.owner,
            admin.administrative,
            admin.permission,
            currAdmin.adminId!!
        )) {
            is Result.Success -> ResponseEntity(updateRes.value, HttpStatus.OK)
            is Result.Error -> ResponseEntity(Err(updateRes.reason), HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("/{adminId}")
    fun deleteAdmin(@PathVariable adminId: Int, request: HttpServletRequest): ResponseEntity<*> {
        val currAdmin =
            extractAdminFromRequest(request) ?: return ResponseEntity(Err("Admin not found"), HttpStatus.UNAUTHORIZED)

        return when (val deleteRes = adminService.deleteAdmin(adminId, currAdmin.adminId!!)) {
            is Result.Success -> ResponseEntity(Unit, HttpStatus.NO_CONTENT)
            is Result.Error -> ResponseEntity(Err(deleteRes.reason), HttpStatus.NOT_FOUND)
        }
    }

    @PostMapping("/{adminId}/tokens")
    fun createToken(
        @PathVariable adminId: Int,
        @RequestBody token: String,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        val currAdmin =
            extractAdminFromRequest(request) ?: return ResponseEntity(Err("Admin not found."), HttpStatus.UNAUTHORIZED)

        return when (val tokenRes = adminService.createToken(token, adminId, currAdmin.adminId!!)) {
            is Result.Success -> ResponseEntity(tokenRes.value, HttpStatus.CREATED)
            is Result.Error -> ResponseEntity(Err(tokenRes.reason), HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("/{adminId}/tokens/{tokenValidation}")
    fun deleteToken(
        @PathVariable adminId: Int,
        @PathVariable tokenValidation: String,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        val currAdmin =
            extractAdminFromRequest(request) ?: return ResponseEntity(Err("Admin not found."), HttpStatus.UNAUTHORIZED)

        return when (val deleteRes = adminService.deleteToken(tokenValidation, adminId, currAdmin.adminId!!)) {
            is Result.Success -> ResponseEntity(Unit, HttpStatus.NO_CONTENT)
            is Result.Error -> ResponseEntity(Err(deleteRes.reason), HttpStatus.NOT_FOUND)
        }
    }

    // TODO: Move this to AuthenticationInterceptor
    private fun extractAdminFromRequest(request: HttpServletRequest): Admin? {
        val adminId: Int = request.getAttribute(ADMIN_ID_ATTRIBUTE) as Int? ?: return null

        return adminService.getById(adminId)
    }
}