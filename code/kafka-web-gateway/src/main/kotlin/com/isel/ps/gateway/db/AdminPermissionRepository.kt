package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.GatewayEntities.Companion.AdminPermission
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class AdminPermissionRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(adminPermission: AdminPermission) {
        val sql = "INSERT INTO admin_permission (administrative, client_permission) VALUES (?, ?)"
        jdbcTemplate.update(sql, adminPermission.administrative, adminPermission.clientPermission)
    }

    fun getById(permissionId: Int): AdminPermission? {
        val sql = "SELECT * FROM admin_permission WHERE permission_id = ?"
        return jdbcTemplate.queryForObject(sql) { rs, _ ->
            AdminPermission(
                permissionId = rs.getInt("permission_id"),
                administrative = rs.getBoolean("administrative"),
                clientPermission = rs.getBoolean("client_permission")
            )
        }
    }

    fun delete(permissionId: Int) {
        val sql = "DELETE FROM admin_permission WHERE permission_id = ?"
        jdbcTemplate.update(sql, permissionId)
    }

    fun getAll(): List<AdminPermission> {
        val sql = "SELECT * FROM admin_permission"
        return jdbcTemplate.query(sql) { rs, _ ->
            AdminPermission(
                permissionId = rs.getInt("permission_id"),
                administrative = rs.getBoolean("administrative"),
                clientPermission = rs.getBoolean("client_permission")
            )
        }
    }
}