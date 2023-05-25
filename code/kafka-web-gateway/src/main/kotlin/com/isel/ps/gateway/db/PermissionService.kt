package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.Permission
import java.sql.Connection
import java.sql.DriverManager

class PermissionService(
    private val dbUrl: String,
    private val dbUsername: String,
    private val dbPassword: String
) {
    private val connection: Connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)

    fun createPermission(permission: Permission) {
        val statement = connection.prepareStatement("INSERT INTO permission (user_id, topic, key) VALUES (?, ?, ?)")
        statement.setLong(1, permission.userId)
        statement.setString(2, permission.topic)
        statement.setString(3, permission.key)
        statement.executeUpdate()
    }

    fun getPermissionsByUserId(userId: Long): List<Permission> {
        val statement = connection.prepareStatement("SELECT * FROM permission WHERE user_id = ?")
        statement.setLong(1, userId)
        val resultSet = statement.executeQuery()

        val permissions = mutableListOf<Permission>()
        while (resultSet.next()) {
            val userId = resultSet.getLong("user_id")
            val topic = resultSet.getString("topic")
            val key = resultSet.getString("key")
            permissions.add(Permission(userId, topic, key))
        }

        return permissions
    }

    fun deletePermission(userId: Long, topic: String) {
        val statement = connection.prepareStatement("DELETE FROM permission WHERE user_id = ? AND topic = ?")
        statement.setLong(1, userId)
        statement.setString(2, topic)
        statement.executeUpdate()
    }
}