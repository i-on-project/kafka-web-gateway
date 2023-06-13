package com.isel.ps.gateway.service

import com.isel.ps.gateway.db.SettingRepository
import com.isel.ps.gateway.model.Setting
import com.isel.ps.gateway.model.SettingType
import com.isel.ps.gateway.utils.Result
import com.isel.ps.gateway.utils.Result.Error
import com.isel.ps.gateway.utils.Result.Success
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant

sealed class SettingCreationError {
    object NoAdministrativePrivileges : SettingCreationError()
    object UnsupportedSettingName : SettingCreationError()
    object AdminNotFound : SettingCreationError()
}

typealias SettingCreationResult = Result<SettingCreationError, Setting>

sealed class SettingDeletionError {
    object NoAdministrativePrivileges : SettingDeletionError()
    object AdminNotFound : SettingDeletionError()
    object SettingNotFound : SettingDeletionError()
}

typealias SettingDeletionResult = Result<SettingDeletionError, Unit>

@Service
class SettingService(private val settingRepository: SettingRepository, private val adminService: AdminService) {
    fun createSetting(
        name: String,
        value: String,
        description: String?,
        currAdminId: Int
    ): SettingCreationResult {
        val currAdmin = adminService.getById(currAdminId)
            ?: return Error(SettingCreationError.AdminNotFound, "Admin \"$currAdminId\" not found.")

        if (!currAdmin.administrative) {
            return Error(
                SettingCreationError.NoAdministrativePrivileges,
                "Admin \"$currAdminId\" has no administrative privileges."
            )
        }

        if (!checkEnumSettingNameExists(name)) {
            return Error(SettingCreationError.UnsupportedSettingName, "Setting name \"$name\" not supported.")
        }

        val setting = Setting(
            name = name,
            value = value,
            description = description,
            updatedAt = Timestamp.from(Instant.now())
        )

        settingRepository.create(setting)

        return Success(setting)
    }

    fun createOwnerSetting(
        name: String,
        value: String,
        description: String?
    ): SettingCreationResult {

        if (!checkEnumSettingNameExists(name)) {
            return Error(SettingCreationError.UnsupportedSettingName, "Setting name \"$name\" not supported.")
        }

        val setting = Setting(
            name = name,
            value = value,
            description = description,
            updatedAt = Timestamp.from(Instant.now())
        )

        settingRepository.create(setting)

        return Success(setting)
    }

    fun createOrUpdateSetting(
        name: String,
        value: String,
        description: String?,
        currAdminId: Int
    ): SettingCreationResult {
        val currAdmin = adminService.getById(currAdminId)
            ?: return Error(SettingCreationError.AdminNotFound, "Admin \"$currAdminId\" not found.")

        if (!currAdmin.administrative) {
            return Error(
                SettingCreationError.NoAdministrativePrivileges,
                "Admin \"$currAdminId\" has no administrative privileges."
            )
        }

        if (!checkEnumSettingNameExists(name)) {
            return Error(SettingCreationError.UnsupportedSettingName, "Setting name \"$name\" not supported.")
        }

        val existingSetting = settingRepository.getBySettingName(name)

        val setting = if (existingSetting == null) {
            Setting(
                name = name,
                value = value,
                description = description,
                updatedAt = Timestamp.from(Instant.now())
            )
        } else {
            existingSetting.value = value
            existingSetting.description = description
            existingSetting
        }

        if (existingSetting == null) {
            settingRepository.create(setting)
        } else {
            settingRepository.update(setting)
        }

        return Success(setting)
    }

    fun getBySettingName(settingName: String): Setting? {
        return settingRepository.getBySettingName(settingName)
    }

    fun deleteSetting(name: String, currAdminId: Int): SettingDeletionResult {
        val currAdmin = adminService.getById(currAdminId)
            ?: return Error(SettingDeletionError.AdminNotFound, "Admin \"$currAdminId\" not found.")

        if (!currAdmin.administrative) {
            return Error(
                SettingDeletionError.NoAdministrativePrivileges,
                "Admin \"$currAdminId\" has no administrative privileges."
            )
        }

        val existingSetting = settingRepository.getBySettingName(name)
            ?: return Error(SettingDeletionError.SettingNotFound, "Setting \"$name\" not found.")

        settingRepository.delete(existingSetting.name)

        return Success(Unit)
    }

    private fun checkEnumSettingNameExists(settingName: String): Boolean {
        for (enumValue in SettingType.values()) {
            if (enumValue.settingName == settingName) {
                return true
            }
        }
        return false
    }
}