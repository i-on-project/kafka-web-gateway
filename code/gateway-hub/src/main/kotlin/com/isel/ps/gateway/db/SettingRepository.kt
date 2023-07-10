package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.Setting
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Repository

@Repository
class SettingRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(setting: Setting) {
        val sql =
            "INSERT INTO setting (name, value, description, updated_at) VALUES (?, ?, ?, ?)"
        jdbcTemplate.update(
            sql,
            setting.name,
            setting.value,
            setting.description,
            setting.updatedAt
        )
    }

    fun getBySettingName(settingName: String): Setting? {
        val sql = "SELECT * FROM setting WHERE name = ?"
        return try {
            jdbcTemplate.queryForObject(sql, settingName) { rs, _ ->
                Setting(
                    name = rs.getString("name"),
                    value = rs.getString("value"),
                    description = rs.getString("description"),
                    updatedAt = rs.getTimestamp("updated_at")
                )
            }
        } catch (err: IncorrectResultSizeDataAccessException) {
            return null
        }
    }

    fun update(setting: Setting) {
        val sql = "UPDATE setting SET value = ?, description = ?, updated_at = ? WHERE name = ?"
        jdbcTemplate.update(
            sql,
            setting.value,
            setting.description,
            setting.updatedAt,
            setting.name
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
                name = rs.getString("name"),
                value = rs.getString("value"),
                description = rs.getString("description"),
                updatedAt = rs.getTimestamp("updated_at")
            )
        }
    }
}