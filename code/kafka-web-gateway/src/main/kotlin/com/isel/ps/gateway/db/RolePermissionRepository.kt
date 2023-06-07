package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.RolePermission
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class RolePermissionRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(rolePermission: RolePermission): RolePermission {
        val sql = "INSERT INTO role_permission (role_id, permission_id) VALUES (?, ?)"
        jdbcTemplate.update(sql, rolePermission.roleId, rolePermission.permissionId)
        return rolePermission
    }

    fun delete(roleId: Int, permissionId: Int) {
        val sql = "DELETE FROM role_permission WHERE role_id = ? AND permission_id = ?"
        jdbcTemplate.update(sql, roleId, permissionId)
    }

    fun getAll(): List<RolePermission> {
        val sql = "SELECT * FROM role_permission"
        return jdbcTemplate.query(sql) { rs, _ ->
            RolePermission(
                roleId = rs.getInt("role_id"),
                permissionId = rs.getInt("permission_id")
            )
        }
    }
}