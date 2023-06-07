package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.AdminToken
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class AdminTokenRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(adminToken: AdminToken) {
        val sql = "INSERT INTO admin_token (token_validation, admin_id, created_at, last_used_at) VALUES (?, ?, ?, ?)"
        jdbcTemplate.update(
            sql,
            adminToken.tokenValidation,
            adminToken.adminId,
            adminToken.createdAt,
            adminToken.lastUsedAt
        )
    }

    fun getByTokenValidation(tokenValidation: String): AdminToken? {
        val sql = "SELECT * FROM admin_token WHERE token_validation = ?"
        return jdbcTemplate.queryForObject(sql) { rs, _ ->
            AdminToken(
                tokenValidation = rs.getString("token_validation"),
                adminId = rs.getInt("admin_id"),
                createdAt = rs.getTimestamp("created_at"),
                lastUsedAt = rs.getTimestamp("last_used_at")
            )
        }
    }

    fun delete(tokenValidation: String) {
        val sql = "DELETE FROM admin_token WHERE token_validation = ?"
        jdbcTemplate.update(sql, tokenValidation)
    }

    fun getAll(): List<AdminToken> {
        val sql = "SELECT * FROM admin_token"
        return jdbcTemplate.query(sql) { rs, _ ->
            AdminToken(
                tokenValidation = rs.getString("token_validation"),
                adminId = rs.getInt("admin_id"),
                createdAt = rs.getTimestamp("created_at"),
                lastUsedAt = rs.getTimestamp("last_used_at")
            )
        }
    }
}