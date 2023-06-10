package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.RolePermission
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Repository
import java.sql.ResultSet


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

    fun exists(roleId: Int, permissionId: Int): Boolean {
        val sql = "SELECT * FROM role_permission WHERE role_id = ? AND permission_id = ?"

        return try {
            jdbcTemplate.queryForObject(sql, roleId, permissionId) { rs, _ ->
                rolePermissionMapper(rs)
            }
            true
        } catch (_: IncorrectResultSizeDataAccessException) {
            false
        }
    }

    fun getAll(): List<RolePermission> {
        val sql = "SELECT * FROM role_permission"
        return jdbcTemplate.query(sql) { rs, _ ->
            rolePermissionMapper(rs)
        }
    }

    private fun rolePermissionMapper(rs: ResultSet) = RolePermission(
        roleId = rs.getInt("role_id"),
        permissionId = rs.getInt("permission_id")
    )
}