package com.isel.ps.gateway.config

import com.isel.ps.gateway.model.Admin
import com.isel.ps.gateway.model.SettingType
import com.isel.ps.gateway.service.*
import com.isel.ps.gateway.utils.Result
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class StartupConfig(
    private val adminService: AdminService,
    private val settingService: SettingService
) {
    @Value("\${gateway.config.admin.username:#{null}}")
    private var adminName: String? = null

    @Value("\${gateway.config.admin.token:#{null}}")
    private var adminToken: String? = null

    @Value("\${gateway.config.admin.description:#{null}}")
    private var adminDescription: String? = null

    @Value("\${gateway.config.auth.server:#{null}}")
    private var authServerEndpoint: String? = null

    private val logger: Logger = LoggerFactory.getLogger(StartupConfig::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun initialize() {
        logger.info("Gateway start received")
        attemptToCreateAdminAndToken()
        attemptToCreateSetting()
    }

    private fun attemptToCreateAdminAndToken() {
        if (adminName == null) {
            logger.warn("Property \"gateway.config.admin.username\" not set, ignoring admin and token creation.")
            return
        }

        var admin: Admin? = null

        val adminRes = adminService.createOwner(
            name = adminName!!,
            description = adminDescription
        )

        when (adminRes) {

            is Result.Success -> {
                admin = adminRes.value
                logger.info("Owner \"$adminName\" created.")
            }

            is Result.Error -> {
                logger.warn(
                    "Owner \"$adminName\" not created.\n" +
                            "Reason: ${adminRes.reason}"
                )

                return
            }
        }

        if (adminToken == null) {
            logger.info("Property \"gateway.config.admin.token\" not set, ignoring token creation.")
        }

        when (val tokenRes = adminService.createOwnerToken(adminToken!!, admin.adminId!!)) {

            is Result.Success -> {
                logger.info("Admin token \"$adminToken\" created.")
            }

            is Result.Error -> {
                logger.warn(
                    "Admin token \"$adminToken\" not created.\n" +
                            "Reason: ${tokenRes.reason}"
                )
            }
        }
    }

    private fun attemptToCreateSetting() {
        if (authServerEndpoint == null) {
            logger.warn("Property \"gateway.config.auth.server\" not set, ignoring setting creation.")
            return
        }

        val authServerSetting = settingService.getBySettingName(SettingType.AuthServer.settingName)

        if (authServerSetting == null) {
            settingService.createOwnerSetting(
                SettingType.AuthServer.settingName,
                authServerEndpoint!!,
                "Authentication server endpoint"
            )
        } else {
            logger.warn("Setting \"auth-server\" already exists.")
        }
    }
}
