package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.Gateway
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.Instant

@Repository
class GatewayRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(gateway: Gateway) {
        val sql =
            "INSERT INTO gateway (gateway_id, topic_keys, topic_clients, topic_commands, active, updated_at) VALUES (?, ?, ?, ?, ?, ?)"
        jdbcTemplate.update(
            sql,
            gateway.gatewayId,
            gateway.topicKeys,
            gateway.topicClients,
            gateway.topicCommands,
            gateway.active,
            gateway.updatedAt
        )
    }

    fun getById(gatewayId: Long): Gateway? {
        val sql = "SELECT * FROM gateway WHERE gateway_id = ?"
        return try {
            jdbcTemplate.queryForObject(sql, gatewayId) { rs, _ ->
                Gateway(
                    gatewayId = rs.getLong("gateway_id"),
                    topicKeys = rs.getString("topic_keys"),
                    topicClients = rs.getString("topic_clients"),
                    topicCommands = rs.getString("topic_commands"),
                    active = rs.getBoolean("active"),
                    updatedAt = rs.getTimestamp("updated_at")
                )
            }
        } catch (err: IncorrectResultSizeDataAccessException) {
            return null
        }
    }

    fun updateActiveGateway(active: Boolean, gatewayId: Long) {
        val sql = "UPDATE gateway SET active = ?, updated_at = ? WHERE gateway_id = ?"
        jdbcTemplate.update(
            sql,
            active,
            Timestamp.from(Instant.now()),
            gatewayId
        )
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
                topicKeys = rs.getString("topic_keys"),
                topicClients = rs.getString("topic_clients"),
                topicCommands = rs.getString("topic_commands"),
                active = rs.getBoolean("active"),
                updatedAt = rs.getTimestamp("updated_at")
            )
        }
    }
}