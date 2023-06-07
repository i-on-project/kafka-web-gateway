package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.Role
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.stereotype.Repository

@Repository
class RoleRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(role: Role): Role {
        val sql = "INSERT INTO role (name, description) VALUES (?, ?)"

        val keyHolder: KeyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ connection ->
            val preparedStatement = connection.prepareStatement(sql, arrayOf("role_id"))
            preparedStatement.setString(1, role.name)
            preparedStatement.setString(2, role.description)
            preparedStatement
        }, keyHolder)

        val generatedId = keyHolder.key?.toInt() ?: throw IllegalStateException("Failed to retrieve generated ID")

        return role.copy(roleId = generatedId)
    }

    fun getById(roleId: Int): Role? {
        val sql = "SELECT * FROM role WHERE role_id = ?"
        return jdbcTemplate.queryForObject(sql) { rs, _ ->
            Role(
                roleId = rs.getInt("role_id"),
                name = rs.getString("name"),
                description = rs.getString("description")
            )
        }
    }

    fun delete(roleId: Int) {
        val sql = "DELETE FROM role WHERE role_id = ?"
        jdbcTemplate.update(sql, roleId)
    }

    fun getAll(): List<Role> {
        val sql = "SELECT * FROM role"
        return jdbcTemplate.query(sql) { rs, _ ->
            Role(
                roleId = rs.getInt("role_id"),
                name = rs.getString("name"),
                description = rs.getString("description")
            )
        }
    }
}