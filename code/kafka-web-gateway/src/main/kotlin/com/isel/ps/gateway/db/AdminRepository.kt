package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.GatewayEntities.Companion.Admin
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class AdminRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(admin: Admin) {
        val sql =
            "INSERT INTO admin (username, password_validation, description, owner) VALUES (?, ?, ?, ?)"
        jdbcTemplate.update(
            sql,
            admin.adminId,
            admin.username,
            admin.passwordValidation,
            admin.description,
            admin.owner
        )
    }

    fun getById(adminId: Int): Admin? {
        val sql = "SELECT * FROM admin WHERE admin_id = ?"
        return jdbcTemplate.queryForObject(sql) { rs, _ ->
            Admin(
                adminId = rs.getInt("admin_id"),
                username = rs.getString("username"),
                passwordValidation = rs.getString("password_validation"),
                description = rs.getString("description"),
                owner = rs.getBoolean("owner")
            )
        }
    }

    fun getByUsername(username: String): Admin? {
        val sql = "SELECT * FROM admin WHERE username = ?"
        return jdbcTemplate.queryForObject(sql) { rs, _ ->
            Admin(
                adminId = rs.getInt("admin_id"),
                username = rs.getString("username"),
                passwordValidation = rs.getString("password_validation"),
                description = rs.getString("description"),
                owner = rs.getBoolean("owner")
            )
        }
    }

    fun update(admin: Admin) {
        val sql = "UPDATE admin SET password_validation = ?, description = ?, owner = ? WHERE admin_id = ?"
        jdbcTemplate.update(
            sql,
            admin.passwordValidation,
            admin.description,
            admin.owner,
            admin.adminId
        )
    }

    fun delete(adminId: Int) {
        val sql = "DELETE FROM admin WHERE admin_id = ?"
        jdbcTemplate.update(sql, adminId)
    }

    fun getAll(): List<Admin> {
        val sql = "SELECT * FROM admin"
        return jdbcTemplate.query(sql) { rs, _ ->
            Admin(
                adminId = rs.getInt("admin_id"),
                username = rs.getString("username"),
                passwordValidation = rs.getString("password_validation"),
                description = rs.getString("description"),
                owner = rs.getBoolean("owner")
            )
        }
    }
}