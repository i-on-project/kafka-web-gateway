package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.GatewayEntities.Companion.ClientRoleAssignment
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class ClientRoleAssignmentRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(clientRoleAssignment: ClientRoleAssignment): ClientRoleAssignment {
        val sql = "INSERT INTO client_role_assignment (client_id, role_id) VALUES (?, ?)"
        jdbcTemplate.update(sql, clientRoleAssignment.clientId, clientRoleAssignment.roleId)
        return clientRoleAssignment
    }

    fun delete(clientId: Long, roleId: Int) {
        val sql = "DELETE FROM client_role_assignment WHERE client_id = ? AND role_id = ?"
        jdbcTemplate.update(sql, clientId, roleId)
    }

    fun getAll(): List<ClientRoleAssignment> {
        val sql = "SELECT * FROM client_role_assignment"
        return jdbcTemplate.query(sql) { rs, _ ->
            ClientRoleAssignment(
                clientId = rs.getLong("client_id"),
                roleId = rs.getInt("role_id")
            )
        }
    }
}