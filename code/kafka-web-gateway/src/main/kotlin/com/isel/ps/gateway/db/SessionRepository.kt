package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.Session
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant

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

    fun getById(sessionId: String): Session? {
        val sql = "SELECT * FROM session WHERE session_id = ?"
        return try {
            jdbcTemplate.queryForObject(sql, sessionId) { rs, _ ->
                sessionMapper(rs)
            }
        } catch (_: IncorrectResultSizeDataAccessException) {
            return null
        }
    }

    fun getByClientId(clientId: String): Session? {
        val sql = "SELECT * FROM session WHERE client_id = ?"
        return try {
            jdbcTemplate.queryForObject(sql, clientId) { rs, _ ->
                sessionMapper(rs)
            }
        } catch (_: IncorrectResultSizeDataAccessException) {
            return null
        }
    }

    fun updateActiveSession(active: Boolean, sessionId: String) {
        val sql = "UPDATE session SET active = ?, updated_at = ? WHERE session_id = ?"
        jdbcTemplate.update(
            sql,
            active,
            Timestamp.from(Instant.now()),
            sessionId
        )
    }

    fun delete(sessionId: String) {
        val sql = "DELETE FROM session WHERE session_id = ?"
        jdbcTemplate.update(sql, sessionId)
    }

    fun getAll(): List<Session> {
        val sql = "SELECT * FROM session"
        return jdbcTemplate.query(sql) { rs, _ ->
            sessionMapper(rs)
        }
    }

    private fun sessionMapper(rs: ResultSet) = Session(
        sessionId = rs.getString("session_id"),
        clientId = rs.getString("client_id"),
        gatewayId = rs.getLong("gateway_id"),
        createdAt = rs.getTimestamp("created_at"),
        updatedAt = rs.getTimestamp("updated_at"),
        active = rs.getBoolean("active")
    )
}
