package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.GatewayEntities.Companion.ClientPermission
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.PreparedStatementCreator
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.PreparedStatement
import java.sql.Statement

@Repository
class ClientPermissionRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(clientPermission: ClientPermission): Int {
        val sql = "INSERT INTO client_permission (topic, key, read, write) VALUES (?, ?, ?, ?)"

        val keyHolder = GeneratedKeyHolder()

        val preparedStatementCreator = PreparedStatementCreator { connection ->
            val preparedStatement: PreparedStatement =
                connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            preparedStatement.setString(1, clientPermission.topic)
            preparedStatement.setString(2, clientPermission.key)
            preparedStatement.setBoolean(3, clientPermission.read)
            preparedStatement.setBoolean(4, clientPermission.write)

            preparedStatement
        }

        jdbcTemplate.update(preparedStatementCreator, keyHolder)

        return keyHolder.key?.toInt() ?: throw IllegalStateException("Failed to retrieve generated ID")
    }

    fun getById(permissionId: Int): ClientPermission? {
        val sql = "SELECT * FROM client_permission WHERE permission_id = ?"
        return jdbcTemplate.queryForObject(sql) { rs, _ ->
            ClientPermission(
                permissionId = rs.getInt("permission_id"),
                topic = rs.getString("topic"),
                key = rs.getString("key"),
                read = rs.getBoolean("read"),
                write = rs.getBoolean("write")
            )
        }
    }

    fun delete(permissionId: Int) {
        val sql = "DELETE FROM client_permission WHERE permission_id = ?"
        jdbcTemplate.update(sql, permissionId)
    }

    fun getAll(): List<ClientPermission> {
        val sql = "SELECT * FROM client_permission"
        return jdbcTemplate.query(sql) { rs, _ ->
            ClientPermission(
                permissionId = rs.getInt("permission_id"),
                topic = rs.getString("topic"),
                key = rs.getString("key"),
                read = rs.getBoolean("read"),
                write = rs.getBoolean("write")
            )
        }
    }
}