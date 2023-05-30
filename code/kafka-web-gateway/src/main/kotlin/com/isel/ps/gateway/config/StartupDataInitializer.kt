package com.isel.ps.gateway.config

import com.isel.ps.gateway.db.AdminRepository
import com.isel.ps.gateway.db.SettingRepository
import com.isel.ps.gateway.model.GatewayEntities.Companion.Admin
import com.isel.ps.gateway.model.GatewayEntities.Companion.Setting
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.time.Instant

@Component
class StartupDataInitializer(
    private val adminRepository: AdminRepository,
    private val settingRepository: SettingRepository
) {
    @Value("\${gateway.config.admin.username}")
    private lateinit var adminUsername: String

    @Value("\${gateway.config.admin.password}")
    private lateinit var adminPassword: String

    @Value("\${gateway.config.admin.description}")
    private lateinit var adminDescription: String

    @Value("\${gateway.config.auth.server}")
    private lateinit var authServerEndpoint: String

    @EventListener(ApplicationReadyEvent::class)
    fun initialize() {
        createOrUpdateAdmin()
        createOrUpdateSettings()
    }

    private fun createOrUpdateAdmin() {
        val existingAdmin = adminRepository.getByUsername(adminUsername)
        if (existingAdmin != null) {
            existingAdmin.passwordValidation = adminPassword
            adminRepository.update(existingAdmin)
        } else {
            val newAdmin = Admin(
                username = adminUsername,
                passwordValidation = adminPassword,
                description = adminDescription,
                owner = true,
                adminId = null
            )
            adminRepository.create(newAdmin)
        }
    }

    private fun createOrUpdateSettings() {
        val existingSettings = settingRepository.getAll()
        val updatedSettings = mutableListOf<Setting>()

        // Check if each setting exists and update it if found, otherwise create a new setting
        if (existingSettings.any { it.settingName == "gateway.config.auth.server" }) {
            val authServerSetting = existingSettings.find { it.settingName == "gateway.config.auth.server" }!!
            authServerSetting.settingValue = authServerEndpoint
            updatedSettings.add(authServerSetting)
        } else {
            val authServerSetting = Setting(
                settingName = "gateway.config.auth.server",
                settingValue = authServerEndpoint,
                settingDescription = "Authentication server endpoint",
                updatedAt = Timestamp.from(Instant.now())
            )
            updatedSettings.add(authServerSetting)
        }

        // Insert or update the modified settings
        updatedSettings.forEach { setting ->
            if (existingSettings.any { it.settingName == setting.settingName }) {
                settingRepository.update(setting)
            } else {
                settingRepository.create(setting)
            }
        }
    }
}