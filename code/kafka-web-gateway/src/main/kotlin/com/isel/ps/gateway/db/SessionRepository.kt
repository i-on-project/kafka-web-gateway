package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.Session
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Repository

@Repository
class SessionRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(session: Session) {
        val sql =
            "INSERT INTO session (session_id, client_id, gateway_id, created_at, updated_at, active) VALUES (?, ?, ?, ?, ?, ?)"
        jdbcTemplate.update(
            sql,
            session.sessionId,
            session.clientId,
            session.gatewayId,
            session.createdAt,
            session.updatedAt,
            session.active
        )
    }

    fun getById(sessionId: Long): Session? {
        val sql = "SELECT * FROM session WHERE session_id = ?"
        return try {
            jdbcTemplate.queryForObject(sql, sessionId) { rs, _ ->
                Session(
                    sessionId = rs.getLong("session_id"),
                    clientId = rs.getLong("client_id"),
                    gatewayId = rs.getLong("gateway_id"),
                    createdAt = rs.getTimestamp("created_at"),
                    updatedAt = rs.getTimestamp("updated_at"),
                    active = rs.getBoolean("active")
                )
            }
        } catch (_: IncorrectResultSizeDataAccessException) {
            return null
        }
    }

    fun delete(sessionId: Long) {
        val sql = "DELETE FROM session WHERE session_id = ?"
        jdbcTemplate.update(sql, sessionId)
    }

    fun getAll(): List<Session> {
        val sql = "SELECT * FROM session"
        return jdbcTemplate.query(sql) { rs, _ ->
            Session(
                sessionId = rs.getLong("session_id"),
                clientId = rs.getLong("client_id"),
                gatewayId = rs.getLong("gateway_id"),
                createdAt = rs.getTimestamp("created_at"),
                updatedAt = rs.getTimestamp("updated_at"),
                active = rs.getBoolean("active")
            )
        }
    }
}
