package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.GatewayEntities.Companion.AdminRole
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class AdminRoleRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(adminRole: AdminRole) {
        val sql = "INSERT INTO admin_role (role, description) VALUES (?, ?)"
        jdbcTemplate.update(sql, adminRole.roleId, adminRole.role, adminRole.description)
    }

    fun getById(roleId: Int): AdminRole? {
        val sql = "SELECT * FROM admin_role WHERE role_id = ?"
        return jdbcTemplate.queryForObject(sql) { rs, _ ->
            AdminRole(
                roleId = rs.getInt("role_id"),
                role = rs.getString("role"),
                description = rs.getString("description")
            )
        }
    }

    fun delete(roleId: Int) {
        val sql = "DELETE FROM admin_role WHERE role_id = ?"
        jdbcTemplate.update(sql, roleId)
    }

    fun getAll(): List<AdminRole> {
        val sql = "SELECT * FROM admin_role"
        return jdbcTemplate.query(sql) { rs, _ ->
            AdminRole(
                roleId = rs.getInt("role_id"),
                role = rs.getString("role"),
                description = rs.getString("description")
            )
        }
    }
}