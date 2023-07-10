package com.isel.ps.chatroom

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ChatroomBackend

fun main(args: Array<String>) {
	runApplication<ChatroomBackend>(*args)
}
