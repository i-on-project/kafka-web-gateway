package com.isel.ps.gateway.controller

import com.isel.ps.gateway.model.Admin
import com.isel.ps.gateway.model.Err
import com.isel.ps.gateway.model.Setting
import com.isel.ps.gateway.service.AdminService
import com.isel.ps.gateway.service.SettingService
import com.isel.ps.gateway.utils.Result
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/setting")
class SettingController(private val settingService: SettingService, private val adminService: AdminService) {

    @PostMapping
    fun createSetting(@RequestBody setting: Setting, request: HttpServletRequest): ResponseEntity<*> {
        val currAdmin =
            extractAdminFromRequest(request) ?: return ResponseEntity(Err("Admin not found"), HttpStatus.UNAUTHORIZED)

        return when (val updatedRes = settingService.createSetting(
            setting.name,
            setting.value,
            setting.description,
            currAdmin.adminId!!
        )) {
            is Result.Success -> ResponseEntity(updatedRes.value, HttpStatus.OK)
            is Result.Error -> ResponseEntity(Err(updatedRes.reason), HttpStatus.BAD_REQUEST)
        }
    }

    @PutMapping("/{name}")
    fun updateSetting(
        @PathVariable name: String,
        @RequestBody setting: Setting,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        val currAdmin =
            extractAdminFromRequest(request) ?: return ResponseEntity(Err("Admin not found"), HttpStatus.UNAUTHORIZED)

        return when (val updatedRes = settingService.createOrUpdateSetting(
            name,
            setting.value,
            setting.description,
            currAdmin.adminId!!
        )) {
            is Result.Success -> ResponseEntity(updatedRes.value, HttpStatus.OK)
            is Result.Error -> ResponseEntity(Err(updatedRes.reason), HttpStatus.BAD_REQUEST)
        }
    }

    @DeleteMapping("/{name}")
    fun deleteSetting(
        @PathVariable name: String,
        request: HttpServletRequest
    ): ResponseEntity<*> {
        val currAdmin =
            extractAdminFromRequest(request) ?: return ResponseEntity(Err("Admin not found"), HttpStatus.UNAUTHORIZED)

        return when (val deleteRes = settingService.deleteSetting(name, currAdmin.adminId!!)) {
            is Result.Success -> ResponseEntity(Unit, HttpStatus.NO_CONTENT)
            is Result.Error -> ResponseEntity(Err(deleteRes.reason), HttpStatus.NOT_FOUND)
        }
    }

    private fun extractAdminFromRequest(request: HttpServletRequest): Admin? {
        val adminId: Int = request.getAttribute("adminId") as Int? ?: return null
        return adminService.getById(adminId)
    }
}