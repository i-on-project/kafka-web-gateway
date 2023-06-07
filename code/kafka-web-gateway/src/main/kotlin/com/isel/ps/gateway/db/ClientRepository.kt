package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.Client
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class ClientRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(client: Client) {
        val sql = "INSERT INTO client (client_id) VALUES (?)"
        jdbcTemplate.update(sql, client.clientId)
    }

    fun delete(clientId: Long) {
        val sql = "DELETE FROM client WHERE client_id = ?"
        jdbcTemplate.update(sql, clientId)
    }

    fun getAll(): List<Client> {
        val sql = "SELECT * FROM client"
        return jdbcTemplate.query(sql) { rs, _ ->
            Client(
                clientId = rs.getLong("client_id")
            )
        }
    }
}
