package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.GatewayEntities.Companion.Admin
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.stereotype.Repository

@Repository
class AdminRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(admin: Admin): Admin {
        val sql = "INSERT INTO admin (username, password_validation, description, owner) VALUES (?, ?, ?, ?)"

        val keyHolder: KeyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ connection ->
            val preparedStatement = connection.prepareStatement(sql, arrayOf("admin_id"))
            preparedStatement.setString(1, admin.username)
            preparedStatement.setString(2, admin.passwordValidation)
            preparedStatement.setString(3, admin.description)
            preparedStatement.setBoolean(4, admin.owner)
            preparedStatement
        }, keyHolder)

        val generatedId = keyHolder.key?.toInt() ?: throw IllegalStateException("Failed to retrieve generated ID")

        return admin.copy(adminId = generatedId)
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