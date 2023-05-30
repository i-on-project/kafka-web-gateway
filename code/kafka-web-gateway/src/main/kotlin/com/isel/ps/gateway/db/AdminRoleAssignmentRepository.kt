package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.GatewayEntities.Companion.AdminRoleAssignment
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class AdminRoleAssignmentRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(adminRoleAssignment: AdminRoleAssignment) {
        val sql = "INSERT INTO admin_role_assignment (admin_id, role_id) VALUES (?, ?)"
        jdbcTemplate.update(sql, adminRoleAssignment.adminId, adminRoleAssignment.roleId)
    }

    fun delete(adminId: Int, roleId: Int) {
        val sql = "DELETE FROM admin_role_assignment WHERE admin_id = ? AND role_id = ?"
        jdbcTemplate.update(sql, adminId, roleId)
    }

    fun getAll(): List<AdminRoleAssignment> {
        val sql = "SELECT * FROM admin_role_assignment"
        return jdbcTemplate.query(sql) { rs, _ ->
            AdminRoleAssignment(
                adminId = rs.getInt("admin_id"),
                roleId = rs.getInt("role_id")
            )
        }
    }
}