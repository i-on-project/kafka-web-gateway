package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.Permission
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.PreparedStatementCreator
import org.springframework.jdbc.core.queryForObject
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.PreparedStatement
import java.sql.Statement

@Repository
class PermissionRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(permission: Permission): Int {
        val sql = "INSERT INTO permission (topic, key, read, write) VALUES (?, ?, ?, ?)"

        val keyHolder = GeneratedKeyHolder()

        val preparedStatementCreator = PreparedStatementCreator { connection ->
            val preparedStatement: PreparedStatement =
                connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            preparedStatement.setString(1, permission.topic)
            preparedStatement.setString(2, permission.key)
            preparedStatement.setBoolean(3, permission.read)
            preparedStatement.setBoolean(4, permission.write)

            preparedStatement
        }

        jdbcTemplate.update(preparedStatementCreator, keyHolder)

        return keyHolder.key?.toInt() ?: throw IllegalStateException("Failed to retrieve generated ID")
    }

    fun getById(permissionId: Int): Permission? {
        val sql = "SELECT * FROM client_permission WHERE permission_id = ?"
        return try {
            jdbcTemplate.queryForObject(sql, permissionId) { rs, _ ->
                Permission(
                    permissionId = rs.getInt("permission_id"),
                    topic = rs.getString("topic"),
                    key = rs.getString("key"),
                    read = rs.getBoolean("read"),
                    write = rs.getBoolean("write")
                )
            }
        } catch (_: IncorrectResultSizeDataAccessException) {
            return null
        }
    }

    fun delete(permissionId: Int) {
        val sql = "DELETE FROM client_permission WHERE permission_id = ?"
        jdbcTemplate.update(sql, permissionId)
    }

    fun getAll(): List<Permission> {
        val sql = "SELECT * FROM client_permission"
        return jdbcTemplate.query(sql) { rs, _ ->
            Permission(
                permissionId = rs.getInt("permission_id"),
                topic = rs.getString("topic"),
                key = rs.getString("key"),
                read = rs.getBoolean("read"),
                write = rs.getBoolean("write")
            )
        }
    }
}