package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.Hub
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.Instant

@Repository
class HubRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(hub: Hub) {
        val sql =
            "INSERT INTO hub (hub_id, topic_keys, topic_clients, topic_commands, active, updated_at) VALUES (?, ?, ?, ?, ?, ?)"
        jdbcTemplate.update(
            sql,
            hub.hubId,
            hub.topicKeys,
            hub.topicClients,
            hub.topicCommands,
            hub.active,
            hub.updatedAt
        )
    }

    fun getById(hubId: Long): Hub? {
        val sql = "SELECT * FROM hub WHERE hub_id = ?"
        return try {
            jdbcTemplate.queryForObject(sql, hubId) { rs, _ ->
                Hub(
                    hubId = rs.getLong("hub_id"),
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

    fun updateActiveHub(active: Boolean, hubId: Long) {
        val sql = "UPDATE hub SET active = ?, updated_at = ? WHERE hub_id = ?"
        jdbcTemplate.update(
            sql,
            active,
            Timestamp.from(Instant.now()),
            hubId
        )
    }

    fun delete(hubId: Long) {
        val sql = "DELETE FROM hub WHERE hub_id = ?"
        jdbcTemplate.update(sql, hubId)
    }

    fun getAll(): List<Hub> {
        val sql = "SELECT * FROM hub"
        return jdbcTemplate.query(sql) { rs, _ ->
            Hub(
                hubId = rs.getLong("hub_id"),
                topicKeys = rs.getString("topic_keys"),
                topicClients = rs.getString("topic_clients"),
                topicCommands = rs.getString("topic_commands"),
                active = rs.getBoolean("active"),
                updatedAt = rs.getTimestamp("updated_at")
            )
        }
    }
}