package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.ClientRole
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class ClientRoleRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(clientRole: ClientRole): ClientRole {
        val sql = "INSERT INTO client_role (client_id, role_id) VALUES (?, ?)"
        jdbcTemplate.update(sql, clientRole.clientId, clientRole.roleId)
        return clientRole
    }

    fun exists(clientId: Long, roleId: Int): Boolean {
        val sql = "SELECT * FROM client_role WHERE client_id = ? AND role_id = ?"

        return try {
            jdbcTemplate.queryForObject(sql, clientId, roleId) { rs, _ ->
                clientRoleMapper(rs)
            }
            true
        } catch (_: IncorrectResultSizeDataAccessException) {
            false
        }
    }

    fun delete(clientId: Long, roleId: Int) {
        val sql = "DELETE FROM client_role WHERE client_id = ? AND role_id = ?"
        jdbcTemplate.update(sql, clientId, roleId)
    }

    fun getAll(): List<ClientRole> {
        val sql = "SELECT * FROM client_role"
        return jdbcTemplate.query(sql) { rs, _ ->
            clientRoleMapper(rs)
        }
    }

    private fun clientRoleMapper(rs: ResultSet) = ClientRole(
        clientId = rs.getLong("client_id"),
        roleId = rs.getInt("role_id")
    )
}