package com.isel.ps.gateway.service

import com.isel.ps.gateway.db.SettingRepository
import com.isel.ps.gateway.model.GatewayEntities.Companion.Setting
import com.isel.ps.gateway.model.GatewayEntities.Companion.SettingType
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant

@Service
class SettingService(private val settingRepository: SettingRepository) {
    fun createSetting(settingName: String, settingValue: String, settingDescription: String?): Setting {

        if (!checkEnumSettingNameExists(settingName)) {
            throw Exception("Setting name($settingName) not supported.")
        }

        val setting = Setting(
            settingName,
            settingValue,
            settingDescription,
            Timestamp.from(Instant.now())
        )

        settingRepository.create(setting)
        return setting
    }

    fun createOrUpdateSetting(settingName: String, settingValue: String, settingDescription: String?): Setting {

        if (!checkEnumSettingNameExists(settingName)) {
            throw Exception("Setting name($settingName) not supported.")
        }

        val setting = Setting(
            settingName,
            settingValue,
            settingDescription,
            Timestamp.from(Instant.now())
        )

        if (settingRepository.getBySettingName(settingName) == null) {
            settingRepository.create(setting)
        } else {
            settingRepository.update(setting)
        }
        return setting
    }

    fun getBySettingName(settingName: String): Setting? {
        return settingRepository.getBySettingName(settingName)
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