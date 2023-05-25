package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.*
import java.sql.Connection
import java.sql.DriverManager

class SessionService(
    private val dbUrl: String,
    private val dbUsername: String,
    private val dbPassword: String
) {
    private val connection: Connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)

    fun createSession(session: Session) {
        // Check if user already has a session
        val existingSession = getSessionByUserId(session.userId)
        if (existingSession != null) {
            // Replace existing session with new one
            deleteSession(existingSession.sessionId)
        }

        // Insert new session
        val statement = connection.prepareStatement("INSERT INTO sessions (session_id, user_id, created_at, gateway_topic) VALUES (?, ?, ?, ?)")
        statement.setLong(1, session.sessionId)
        statement.setLong(2, session.userId)
        statement.setTimestamp(3, session.createdAt)
        statement.setString(4, session.gatewayTopic)
        statement.executeUpdate()
    }

    fun getSession(sessionId: Long): Session? {
        val statement = connection.prepareStatement("SELECT * FROM session WHERE session_id = ?")
        statement.setLong(1, sessionId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val sessionId = resultSet.getLong(SESSION_ID)
            val userId = resultSet.getLong(USER_ID)
            val createdAt = resultSet.getTimestamp(CREATED_AT)
            val gatewayTopic = resultSet.getString(GATEWAY_TOPIC)
            return Session(sessionId, userId, createdAt, gatewayTopic)
        }

        return null
    }

    fun getSessionByUserId(userId: Long): Session? {
        val statement = connection.prepareStatement("SELECT * FROM session WHERE user_id = ?")
        statement.setLong(1, userId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val sessionId = resultSet.getLong(SESSION_ID)
            val userId = resultSet.getLong(USER_ID)
            val createdAt = resultSet.getTimestamp(CREATED_AT)
            val gatewayTopic = resultSet.getString(GATEWAY_TOPIC)
            return Session(sessionId, userId, createdAt, gatewayTopic)
        }

        return null
    }

    fun deleteSession(sessionId: Long) {
        val statement = connection.prepareStatement("DELETE FROM sessions WHERE session_id = ?")
        statement.setLong(1, sessionId)
        statement.executeUpdate()
    }

    private fun convertToSqlArray(strings: Array<String>, connection: Connection): java.sql.Array {
        return connection.createArrayOf("text", strings)
    }

    private fun convertToKotlinArray(sqlArray: java.sql.Array): Array<String> {
        val array = sqlArray.array as Array<*>
        return array.map { it.toString() }.toTypedArray()
    }
}
