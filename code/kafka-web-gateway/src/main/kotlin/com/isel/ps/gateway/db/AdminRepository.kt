package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.Admin
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class AdminRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(admin: Admin): Admin {
        val sql = "INSERT INTO admin (name, description, owner, administrative, permission) VALUES (?, ?, ?, ?, ?)"

        val keyHolder: KeyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ connection ->
            val preparedStatement = connection.prepareStatement(sql, arrayOf("admin_id"))
            preparedStatement.setString(1, admin.name)
            preparedStatement.setString(2, admin.description)
            preparedStatement.setBoolean(3, admin.owner)
            preparedStatement.setBoolean(4, admin.administrative)
            preparedStatement.setBoolean(5, admin.permission)
            preparedStatement
        }, keyHolder)

        val generatedId = keyHolder.key?.toInt() ?: throw IllegalStateException("Failed to retrieve generated ID")

        return admin.copy(adminId = generatedId)
    }

    fun getById(adminId: Int): Admin? {
        val sql = "SELECT * FROM admin WHERE admin_id = ?"
        return try {
            jdbcTemplate.queryForObject(sql, adminId) { rs, _ ->
                adminMapper(rs)
            }
        } catch (_: IncorrectResultSizeDataAccessException) {
            return null
        }
    }

    fun getByName(name: String): Admin? {
        val sql = "SELECT * FROM admin WHERE name = ?"
        return try {
            jdbcTemplate.queryForObject(sql, name) { rs, _ ->
                adminMapper(rs)
            }
        } catch (_: IncorrectResultSizeDataAccessException) {
            return null
        }
    }

    fun ownerExists(): Boolean {
        val sql = "SELECT * FROM admin WHERE owner = true"
        return jdbcTemplate.query(sql) { rs, _ ->
            adminMapper(rs)
        }.size > 0
    }

    fun update(admin: Admin) {
        val sql =
            "UPDATE admin SET name = ?, description = ?, owner = ?, permission = ?, administrative = ? WHERE admin_id = ?"
        jdbcTemplate.update(
            sql,
            admin.name,
            admin.description,
            admin.owner,
            admin.permission,
            admin.administrative,
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
            adminMapper(rs)
        }
    }

    private fun adminMapper(rs: ResultSet) = Admin(
        adminId = rs.getInt("admin_id"),
        name = rs.getString("name"),
        description = rs.getString("description"),
        owner = rs.getBoolean("owner"),
        administrative = rs.getBoolean("administrative"),
        permission = rs.getBoolean("permission")
    )
}