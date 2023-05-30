package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.GatewayEntities.Companion.ActiveSession
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class ActiveSessionRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(activeSession: ActiveSession) {
        val sql = "INSERT INTO active_session (session_id, updated_at) VALUES (?, ?)"
        jdbcTemplate.update(sql, activeSession.sessionId, activeSession.updatedAt)
    }

    fun getById(sessionId: Long): ActiveSession? {
        val sql = "SELECT * FROM active_session WHERE session_id = ?"
        return jdbcTemplate.queryForObject(sql) { rs, _ ->
            ActiveSession(
                sessionId = rs.getLong("session_id"),
                updatedAt = rs.getTimestamp("updated_at")
            )
        }
    }

    fun delete(sessionId: Long) {
        val sql = "DELETE FROM active_session WHERE session_id = ?"
        jdbcTemplate.update(sql, sessionId)
    }

    fun getAll(): List<ActiveSession> {
        val sql = "SELECT * FROM active_session"
        return jdbcTemplate.query(sql) { rs, _ ->
            ActiveSession(
                sessionId = rs.getLong("session_id"),
                updatedAt = rs.getTimestamp("updated_at")
            )
        }
    }
}