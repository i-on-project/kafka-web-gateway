package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.Gateway
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class GatewayRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(gateway: Gateway) {
        val sql =
            "INSERT INTO gateway (gateway_id, topic_clients, topic_commands, active, updated_at) VALUES (?, ?, ?, ?, ?)"
        jdbcTemplate.update(
            sql,
            gateway.gatewayId,
            gateway.topicClients,
            gateway.topicCommands,
            gateway.active,
            gateway.updatedAt
        )
    }

    fun getById(gatewayId: Long): Gateway? {
        val sql = "SELECT * FROM gateway WHERE gateway_id = ?"
        return jdbcTemplate.queryForObject(sql) { rs, _ ->
            Gateway(
                gatewayId = rs.getLong("gateway_id"),
                topicClients = rs.getString("topic_clients"),
                topicCommands = rs.getString("topic_commands"),
                active = rs.getBoolean("active"),
                updatedAt = rs.getTimestamp("updated_at")
            )
        }
    }

    fun delete(gatewayId: Long) {
        val sql = "DELETE FROM gateway WHERE gateway_id = ?"
        jdbcTemplate.update(sql, gatewayId)
    }

    fun getAll(): List<Gateway> {
        val sql = "SELECT * FROM gateway"
        return jdbcTemplate.query(sql) { rs, _ ->
            Gateway(
                gatewayId = rs.getLong("gateway_id"),
                topicClients = rs.getString("topic_clients"),
                topicCommands = rs.getString("topic_commands"),
                active = rs.getBoolean("active"),
                updatedAt = rs.getTimestamp("updated_at")
            )
        }
    }
}