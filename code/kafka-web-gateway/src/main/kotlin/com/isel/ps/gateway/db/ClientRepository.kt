package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.GatewayEntities.Companion.Client
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class ClientRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(client: Client) {
        val sql = "INSERT INTO client (client_id, username) VALUES (?, ?)"
        jdbcTemplate.update(sql, client.clientId, client.username)
    }

    fun getById(clientId: Long): Client? {
        val sql = "SELECT * FROM client WHERE client_id = ?"
        return jdbcTemplate.queryForObject(sql) { rs, _ ->
            Client(
                clientId = rs.getLong("client_id"),
                username = rs.getString("username")
            )
        }
    }

    fun delete(clientId: Long) {
        val sql = "DELETE FROM client WHERE client_id = ?"
        jdbcTemplate.update(sql, clientId)
    }

    fun getAll(): List<Client> {
        val sql = "SELECT * FROM client"
        return jdbcTemplate.query(sql) { rs, _ ->
            Client(
                clientId = rs.getLong("client_id"),
                username = rs.getString("username")
            )
        }
    }
}
