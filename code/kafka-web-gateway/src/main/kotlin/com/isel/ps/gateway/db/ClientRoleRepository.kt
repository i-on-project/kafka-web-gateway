package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.GatewayEntities.Companion.ClientRole
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.stereotype.Repository

@Repository
class ClientRoleRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(clientRole: ClientRole): ClientRole {
        val sql = "INSERT INTO client_role (role, description) VALUES (?, ?)"

        val keyHolder: KeyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ connection ->
            val preparedStatement = connection.prepareStatement(sql, arrayOf("role_id"))
            preparedStatement.setString(1, clientRole.role)
            preparedStatement.setString(2, clientRole.description)
            preparedStatement
        }, keyHolder)

        val generatedId = keyHolder.key?.toInt() ?: throw IllegalStateException("Failed to retrieve generated ID")

        return clientRole.copy(roleId = generatedId)
    }

    fun getById(roleId: Int): ClientRole? {
        val sql = "SELECT * FROM client_role WHERE role_id = ?"
        return jdbcTemplate.queryForObject(sql) { rs, _ ->
            ClientRole(
                roleId = rs.getInt("role_id"),
                role = rs.getString("role"),
                description = rs.getString("description")
            )
        }
    }

    fun delete(roleId: Int) {
        val sql = "DELETE FROM client_role WHERE role_id = ?"
        jdbcTemplate.update(sql, roleId)
    }

    fun getAll(): List<ClientRole> {
        val sql = "SELECT * FROM client_role"
        return jdbcTemplate.query(sql) { rs, _ ->
            ClientRole(
                roleId = rs.getInt("role_id"),
                role = rs.getString("role"),
                description = rs.getString("description")
            )
        }
    }
}