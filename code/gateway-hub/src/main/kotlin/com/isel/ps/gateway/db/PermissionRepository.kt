package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.Permission
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.queryForObject
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class PermissionRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(permission: Permission): Permission {
        val sql = "INSERT INTO permission (topic, key, read, write) VALUES (?, ?, ?, ?)"

        val keyHolder = GeneratedKeyHolder()

        jdbcTemplate.update({ connection ->
            val preparedStatement = connection.prepareStatement(sql, arrayOf("permission_id"))
            preparedStatement.setString(1, permission.topic)
            preparedStatement.setString(2, permission.key)
            preparedStatement.setBoolean(3, permission.read)
            preparedStatement.setBoolean(4, permission.write)

            preparedStatement
        }, keyHolder)

        val generatedId = keyHolder.key?.toInt() ?: throw IllegalStateException("Failed to retrieve generated ID")

        return permission.copy(permissionId = generatedId)
    }

    fun getById(permissionId: Int): Permission? {
        val sql = "SELECT * FROM permission WHERE permission_id = ?"
        return try {
            jdbcTemplate.queryForObject(sql, permissionId) { rs, _ ->
                permissionMapper(rs)
            }
        } catch (_: IncorrectResultSizeDataAccessException) {
            return null
        }
    }

    fun getPermission(permission: Permission): Permission? {
        val query = """
            SELECT *
            FROM permission
            WHERE topic = ? AND (key = ? OR key IS NULL) AND read = ? AND write = ?
        """.trimIndent()

        val rowMapper = RowMapper { rs, _ ->
            permissionMapper(rs)
        }

        val permissionList =
            jdbcTemplate.query(query, rowMapper, permission.topic, permission.key, permission.read, permission.write)

        return permissionList.firstOrNull()
    }

    fun delete(permissionId: Int) {
        val sql = "DELETE FROM permission WHERE permission_id = ?"
        jdbcTemplate.update(sql, permissionId)
    }

    fun getAll(): List<Permission> {
        val sql = "SELECT * FROM permission"
        return jdbcTemplate.query(sql) { rs, _ ->
            permissionMapper(rs)
        }
    }

    private fun permissionMapper(rs: ResultSet) = Permission(
        permissionId = rs.getInt("permission_id"),
        topic = rs.getString("topic"),
        key = rs.getString("key"),
        read = rs.getBoolean("read"),
        write = rs.getBoolean("write")
    )
}