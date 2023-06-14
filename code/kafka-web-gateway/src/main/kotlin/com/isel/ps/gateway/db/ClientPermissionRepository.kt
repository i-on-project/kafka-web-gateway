package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.ClientPermission
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Repository
import java.sql.ResultSet


@Repository
class ClientPermissionRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(clientPermission: ClientPermission): ClientPermission {
        val sql = "INSERT INTO client_permission (client_id, permission_id) VALUES (?, ?)"
        jdbcTemplate.update(sql, clientPermission.clientId, clientPermission.permissionId)
        return clientPermission
    }

    fun delete(clientId: String, permissionId: Int) {
        val sql = "DELETE FROM client_permission WHERE client_id = ? AND permission_id = ?"
        jdbcTemplate.update(sql, clientId, permissionId)
    }

    fun exists(clientId: String, permissionId: Int): Boolean {
        val sql = "SELECT * FROM client_permission WHERE client_id = ? AND permission_id = ?"

        return try {
            jdbcTemplate.queryForObject(sql, clientId, permissionId) { rs, _ ->
                clientPermissionMapper(rs)
            }
            true
        } catch (_: IncorrectResultSizeDataAccessException) {
            false
        }
    }

    fun getAll(): List<ClientPermission> {
        val sql = "SELECT * FROM client_permission"
        return jdbcTemplate.query(sql) { rs, _ ->
            clientPermissionMapper(rs)
        }
    }

    private fun clientPermissionMapper(rs: ResultSet) = ClientPermission(
        clientId = rs.getString("client_id"),
        permissionId = rs.getInt("permission_id")
    )
}