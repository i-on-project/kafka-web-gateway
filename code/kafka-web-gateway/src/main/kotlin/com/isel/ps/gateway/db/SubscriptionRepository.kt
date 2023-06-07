package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.Subscription
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class SubscriptionRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(subscription: Subscription) {
        val sql = "INSERT INTO subscription (session_id, topic, key) VALUES (?, ?, ?)"
        jdbcTemplate.update(sql, subscription.sessionId, subscription.topic, subscription.key)
    }

    fun getById(subscriptionId: Int): Subscription? {
        val sql = "SELECT * FROM subscription WHERE subscription_id = ?"
        return jdbcTemplate.queryForObject(sql) { rs, _ ->
            Subscription(
                subscriptionId = rs.getInt("subscription_id"),
                sessionId = rs.getLong("session_id"),
                topic = rs.getString("topic"),
                key = rs.getString("key")
            )
        }
    }

    fun delete(subscriptionId: Int) {
        val sql = "DELETE FROM subscription WHERE subscription_id = ?"
        jdbcTemplate.update(sql, subscriptionId)
    }

    fun getAll(): List<Subscription> {
        val sql = "SELECT * FROM subscription"
        return jdbcTemplate.query(sql) { rs, _ ->
            Subscription(
                subscriptionId = rs.getInt("subscription_id"),
                sessionId = rs.getLong("session_id"),
                topic = rs.getString("topic"),
                key = rs.getString("key")
            )
        }
    }
}