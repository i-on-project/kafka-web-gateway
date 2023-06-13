package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.Subscription
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder
import org.springframework.stereotype.Repository

@Repository
class SubscriptionRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(subscription: Subscription): Subscription {
        val sql = "INSERT INTO subscription (session_id, topic, key) VALUES (?, ?, ?)"
        jdbcTemplate.update(sql, subscription.sessionId, subscription.topic, subscription.key)

        val keyHolder: KeyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ connection ->
            val preparedStatement = connection.prepareStatement(sql, arrayOf("subscription_id"))
            preparedStatement.setString(1, subscription.sessionId)
            preparedStatement.setString(1, subscription.topic)
            preparedStatement.setString(1, subscription.key)
            preparedStatement
        }, keyHolder)

        val generatedId = keyHolder.key?.toInt() ?: throw IllegalStateException("Failed to retrieve generated ID")

        return subscription.copy(subscriptionId = generatedId)
    }

    fun getById(subscriptionId: Int): Subscription? {
        val sql = "SELECT * FROM subscription WHERE subscription_id = ?"
        return try {
            jdbcTemplate.queryForObject(sql, subscriptionId) { rs, _ ->
                Subscription(
                    subscriptionId = rs.getInt("subscription_id"),
                    sessionId = rs.getString("session_id"),
                    topic = rs.getString("topic"),
                    key = rs.getString("key")
                )
            }
        } catch (_: IncorrectResultSizeDataAccessException) {
            return null
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
                sessionId = rs.getString("session_id"),
                topic = rs.getString("topic"),
                key = rs.getString("key")
            )
        }
    }
}