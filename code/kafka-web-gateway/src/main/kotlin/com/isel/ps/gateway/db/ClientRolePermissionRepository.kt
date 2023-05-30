package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.GatewayEntities.Companion.ClientRolePermission
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class ClientRolePermissionRepository(private val jdbcTemplate: JdbcTemplate) {
    fun create(clientRolePermission: ClientRolePermission): ClientRolePermission {
        val sql = "INSERT INTO client_role_permission (role_id, permission_id) VALUES (?, ?)"
        jdbcTemplate.update(sql, clientRolePermission.roleId, clientRolePermission.permissionId)
        return clientRolePermission
    }

    fun delete(roleId: Int, permissionId: Int) {
        val sql = "DELETE FROM client_role_permission WHERE role_id = ? AND permission_id = ?"
        jdbcTemplate.update(sql, roleId, permissionId)
    }

    fun getAll(): List<ClientRolePermission> {
        val sql = "SELECT * FROM client_role_permission"
        return jdbcTemplate.query(sql) { rs, _ ->
            ClientRolePermission(
                roleId = rs.getInt("role_id"),
                permissionId = rs.getInt("permission_id")
            )
        }
    }
}