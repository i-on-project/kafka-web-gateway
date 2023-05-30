package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.GatewayEntities.Companion.AdminRolePermission
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class AdminRolePermissionRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(adminRolePermission: AdminRolePermission) {
        val sql = "INSERT INTO admin_role_permission (role_id, permission_id) VALUES (?, ?)"
        jdbcTemplate.update(sql, adminRolePermission.roleId, adminRolePermission.permissionId)
    }

    fun delete(roleId: Int, permissionId: Int) {
        val sql = "DELETE FROM admin_role_permission WHERE role_id = ? AND permission_id = ?"
        jdbcTemplate.update(sql, roleId, permissionId)
    }

    fun getAll(): List<AdminRolePermission> {
        val sql = "SELECT * FROM admin_role_permission"
        return jdbcTemplate.query(sql) { rs, _ ->
            AdminRolePermission(
                roleId = rs.getInt("role_id"),
                permissionId = rs.getInt("permission_id")
            )
        }
    }
}