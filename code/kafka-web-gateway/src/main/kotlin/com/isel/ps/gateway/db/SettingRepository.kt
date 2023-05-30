package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.GatewayEntities.Companion.Setting
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class SettingRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(setting: Setting) {
        val sql =
            "INSERT INTO setting (setting_name, setting_value, setting_description, updated_at) VALUES (?, ?, ?, ?)"
        jdbcTemplate.update(
            sql,
            setting.settingName,
            setting.settingValue,
            setting.settingDescription,
            setting.updatedAt
        )
    }

    fun getBySettingName(settingName: String): Setting? {
        val sql = "SELECT * FROM setting WHERE setting_name = ?"
        return jdbcTemplate.queryForObject(sql) { rs, _ ->
            Setting(
                settingName = rs.getString("setting_name"),
                settingValue = rs.getString("setting_value"),
                settingDescription = rs.getString("setting_description"),
                updatedAt = rs.getTimestamp("updated_at")
            )
        }
    }

    fun update(setting: Setting) {
        val sql = "UPDATE setting SET setting_value = ?, setting_description = ?, updated_at = ? WHERE setting_name = ?"
        jdbcTemplate.update(
            sql,
            setting.settingValue,
            setting.settingDescription,
            setting.updatedAt,
            setting.settingName
        )
    }

    fun delete(settingName: String) {
        val sql = "DELETE FROM setting WHERE setting_name = ?"
        jdbcTemplate.update(sql, settingName)
    }

    fun getAll(): List<Setting> {
        val sql = "SELECT * FROM setting"
        return jdbcTemplate.query(sql) { rs, _ ->
            Setting(
                settingName = rs.getString("setting_name"),
                settingValue = rs.getString("setting_value"),
                settingDescription = rs.getString("setting_description"),
                updatedAt = rs.getTimestamp("updated_at")
            )
        }
    }
}