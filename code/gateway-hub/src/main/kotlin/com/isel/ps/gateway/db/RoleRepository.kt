package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.Role
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.stereotype.Repository
import java.sql.ResultSet

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
        return try {
            jdbcTemplate.queryForObject(sql, roleId) { rs, _ ->
                roleMapper(rs)
            }
        } catch (_: IncorrectResultSizeDataAccessException) {
            return null
        }
    }

    fun getByName(name: String): Role? {
        val sql = "SELECT * FROM role WHERE name = ?"
        return try {
            jdbcTemplate.queryForObject(sql, name) { rs, _ ->
                roleMapper(rs)
            }
        } catch (_: IncorrectResultSizeDataAccessException) {
            return null
        }
    }

    fun delete(roleId: Int) {
        val sql = "DELETE FROM role WHERE role_id = ?"
        jdbcTemplate.update(sql, roleId)
    }

    fun getAll(): List<Role> {
        val sql = "SELECT * FROM role"
        return jdbcTemplate.query(sql) { rs, _ ->
            roleMapper(rs)
        }
    }

    private fun roleMapper(rs: ResultSet) = Role(
        roleId = rs.getInt("role_id"),
        name = rs.getString("name"),
        description = rs.getString("description")
    )
}