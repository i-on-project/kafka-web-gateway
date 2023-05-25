package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.Subscription
import java.sql.Connection
import java.sql.DriverManager

class SubscriptionService(
    private val dbUrl: String,
    private val dbUsername: String,
    private val dbPassword: String
) {
    private val connection: Connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)

    fun createSubscription(subscription: Subscription) {
        val statement =
            connection.prepareStatement("INSERT INTO subscription (session_id, topic, key) VALUES (?, ?, ?)")
        statement.setLong(1, subscription.sessionId)
        statement.setString(2, subscription.topic)
        statement.setString(3, subscription.key)
        statement.executeUpdate()
    }

    fun getSubscriptionsBySessionId(sessionId: Long): List<Subscription> {
        val statement = connection.prepareStatement("SELECT * FROM subscription WHERE session_id = ?")
        statement.setLong(1, sessionId)
        val resultSet = statement.executeQuery()

        val subscriptions = mutableListOf<Subscription>()
        while (resultSet.next()) {
            val sessionId = resultSet.getLong("session_id")
            val topic = resultSet.getString("topic")
            val key = resultSet.getString("key")
            subscriptions.add(Subscription(sessionId, topic, key))
        }

        return subscriptions
    }

    fun deleteSubscription(sessionId: Long, topic: String) {
        val statement = connection.prepareStatement("DELETE FROM subscription WHERE session_id = ? AND topic = ?")
        statement.setLong(1, sessionId)
        statement.setString(2, topic)
        statement.executeUpdate()
    }
}