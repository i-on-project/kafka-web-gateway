package com.isel.ps.gateway.config

import com.isel.ps.gateway.db.AdminRepository
import com.isel.ps.gateway.model.Admin
import com.isel.ps.gateway.model.SettingType
import com.isel.ps.gateway.service.GatewayService
import com.isel.ps.gateway.service.SettingService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class StartupConfig(
    private val gatewayService: GatewayService,
    private val adminRepository: AdminRepository,
    private val settingService: SettingService
) {
    @Value("\${gateway.config.admin.username}")
    private lateinit var adminName: String

    @Value("\${gateway.config.admin.password}")
    private lateinit var adminPassword: String

    @Value("\${gateway.config.admin.description}")
    private lateinit var adminDescription: String

    @Value("\${gateway.config.auth.server}")
    private lateinit var authServerEndpoint: String

    @EventListener(ApplicationReadyEvent::class)
    fun initialize() {
        createGateway()
        attemptToCreateAdmin()
        attemptToCreateSetting()
    }

    private fun createGateway() {
        GatewayConfig.GATEWAY = gatewayService.createGateway(true)
    }

    private fun attemptToCreateAdmin() {
        val existingAdmin = adminRepository.getByName(adminName)
        if (existingAdmin == null) {
            val newAdmin = Admin(
                name = adminName,
                description = adminDescription,
                owner = true,
                adminId = null,
                permission = true,
                administrative = true
            )
            adminRepository.create(newAdmin)
        }
    }

    private fun attemptToCreateSetting() {
        val authServerSetting = settingService.getBySettingName(SettingType.AuthServer.settingName)

        if (authServerSetting == null) {
            settingService.createSetting(
                SettingType.AuthServer.settingName,
                authServerEndpoint,
                "Authentication server endpoint"
            )
        }
    }
}
