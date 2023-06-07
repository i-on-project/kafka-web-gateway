package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.ClientRole
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class ClientRoleRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(clientRole: ClientRole): ClientRole {
        val sql = "INSERT INTO client_role (client_id, role_id) VALUES (?, ?)"
        jdbcTemplate.update(sql, clientRole.clientId, clientRole.roleId)
        return clientRole
    }

    fun delete(clientId: Long, roleId: Int) {
        val sql = "DELETE FROM client_role WHERE client_id = ? AND role_id = ?"
        jdbcTemplate.update(sql, clientId, roleId)
    }

    fun getAll(): List<ClientRole> {
        val sql = "SELECT * FROM client_role"
        return jdbcTemplate.query(sql) { rs, _ ->
            ClientRole(
                clientId = rs.getLong("client_id"),
                roleId = rs.getInt("role_id")
            )
        }
    }
}