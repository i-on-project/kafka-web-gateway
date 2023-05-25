package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.ActiveSession
import java.sql.Connection
import java.sql.DriverManager

class ActiveSessionService(
    private val dbUrl: String,
    private val dbUsername: String,
    private val dbPassword: String
) {
    private val connection: Connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)

    fun createActiveSession(activeSession: ActiveSession) {
        val statement = connection.prepareStatement("INSERT INTO active_session (session_id, updated_at) VALUES (?, ?)")
        statement.setLong(1, activeSession.sessionId)
        statement.setTimestamp(2, activeSession.updatedAt)
        statement.executeUpdate()
    }

    fun getActiveSession(sessionId: Long): ActiveSession? {
        val statement = connection.prepareStatement("SELECT * FROM active_session WHERE session_id = ?")
        statement.setLong(1, sessionId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val sessionId = resultSet.getLong("session_id")
            val updatedAt = resultSet.getTimestamp("updated_at")
            return ActiveSession(sessionId, updatedAt)
        }

        return null
    }

    fun deleteActiveSession(sessionId: Long) {
        val statement = connection.prepareStatement("DELETE FROM active_session WHERE session_id = ?")
        statement.setLong(1, sessionId)
        statement.executeUpdate()
    }
}