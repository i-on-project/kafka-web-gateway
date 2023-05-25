package com.isel.ps.gateway.db

import com.isel.ps.gateway.model.UserInfo
import java.sql.Connection
import java.sql.DriverManager

class UserInfoService(
    private val dbUrl: String,
    private val dbUsername: String,
    private val dbPassword: String
) {
    private val connection: Connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)

    fun createUser(user: UserInfo) {
        val statement = connection.prepareStatement("INSERT INTO user_info (user_id, username) VALUES (?, ?)")
        statement.setLong(1, user.userId)
        statement.setString(2, user.username)
        statement.executeUpdate()
    }

    fun getUser(userId: Long): UserInfo? {
        val statement = connection.prepareStatement("SELECT * FROM user_info WHERE user_id = ?")
        statement.setLong(1, userId)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val userId = resultSet.getLong("user_id")
            val username = resultSet.getString("username")
            return UserInfo(userId, username)
        }
        return null
    }

    fun deleteUser(userId: Long) {
        val statement = connection.prepareStatement("DELETE FROM user_info WHERE user_id = ?")
        statement.setLong(1, userId)
        statement.executeUpdate()
    }
}