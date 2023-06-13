package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.Client
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Repository
import java.sql.ResultSet

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

    fun exists(clientId: Long): Boolean {
        val sql = "SELECT * FROM client WHERE client_id = ?"

        return try {
            jdbcTemplate.queryForObject(sql, clientId) { rs, _ ->
                clientMapper(rs)
            }
            true
        } catch (_: IncorrectResultSizeDataAccessException) {
            false
        }
    }

    fun getAll(): List<Client> {
        val sql = "SELECT * FROM client"
        return jdbcTemplate.query(sql) { rs, _ ->
            clientMapper(rs)
        }
    }

    private fun clientMapper(rs: ResultSet) = Client(
        clientId = rs.getLong("client_id")
    )
}
