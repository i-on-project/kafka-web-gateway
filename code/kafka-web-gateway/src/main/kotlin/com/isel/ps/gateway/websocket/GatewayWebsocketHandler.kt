package com.isel.ps.gateway.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.isel.ps.gateway.db.SubscriptionRepository
import com.isel.ps.gateway.kafka.RecordDealer
import com.isel.ps.gateway.model.*
import com.isel.ps.gateway.service.ClientService
import com.isel.ps.gateway.service.PermissionValidatorService
import com.isel.ps.gateway.service.SessionService
import com.isel.ps.gateway.websocket.ClientAuthenticationInterceptor.Companion.CLIENT_ID
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.*
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator
import java.io.IOException
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class GatewayWebsocketHandler(
    private val subscriptionRepository: SubscriptionRepository,
    private val sessionsStorage: ClientSessions,
    private val kafkaProducer: KafkaProducer<String, String>,
    private val recordDealer: RecordDealer,
    private val messageStatus: MessageStatus,
    private val clientService: ClientService,
    private val sessionService: SessionService,
    private val permissionValidatorService: PermissionValidatorService
) : WebSocketHandler {

    companion object {
        val objectMapper = ObjectMapper().also {
            val module = SimpleModule()
            module.addDeserializer(Command::class.java, CommandDeserializer())
            module.addDeserializer(TopicType::class.java, TopicTypeDeserializer())
            it.registerModule(module)
        }

        fun json(obj: Any) = TextMessage(objectMapper.writeValueAsString(obj))
    }

    private val pingPongTimers: MutableMap<String, Timer> = ConcurrentHashMap()
    private val logger: Logger = LoggerFactory.getLogger(GatewayWebsocketHandler::class.java)
    // val subscriptions: MutableMap<String, List<TopicType>> = mutableMapOf()

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        if (message is PongMessage) {
            return
        }

        val payload: String
        try {
            payload = message.payload as String
        } catch (ex: Exception) {
            sendErr(ClientMessage(null, Err("")), session, "Invalid payload.")
            return
        }

        val clientMessage: ClientMessage
        try {
            clientMessage = objectMapper.readValue<ClientMessage>(payload)
        } catch (ex: Exception) {
            ex.printStackTrace()
            sendErr(ClientMessage(null, Err("")), session, "Unknown message received.")
            return
        }

        logger.info("[${clientMessage.messageId}] Received $payload")

        val clientId = getClientIdFromSession(session)

        val concurrentSession = ConcurrentWebSocketSessionDecorator(session, 5000, 65536)

        when (clientMessage.command) {
            is Subscribe -> {
                val subscribeCommand = clientMessage.command
                if (permissionValidatorService.hasPermissionForAll(
                        subscribeCommand.topics,
                        clientId,
                        true
                    )
                ) {
                    subscribeCommand.topics.forEach { topic ->
                        val subscription = Subscription(
                            (Math.random() * 10000).toInt(),
                            session.id,
                            topic.topic,
                            topic.key
                        ) //TODO: debate how to input the values..
                        recordDealer.addSubscription(sessionsStorage.getClientSession(session.id)!!, subscription)
                    }
                    sendAck(
                        clientMessage,
                        concurrentSession
                    )
                } else {
                    sendErr(clientMessage, concurrentSession, "Operation not allowed, no permission.")
                }
            }

            is Consume -> {
                val consumeCommand = clientMessage.command
            }

            is Publish -> {
                val publishCommand = clientMessage.command
                val producerRecord = ProducerRecord(publishCommand.topic, publishCommand.key, publishCommand.value)
                if (permissionValidatorService.hasPermission(
                        publishCommand.topic,
                        publishCommand.key,
                        clientId,
                        false
                    )
                ) {
                    kafkaProducer.send(producerRecord) { _, err ->
                        if (err != null) {
                            sendErr(clientMessage, concurrentSession, err.message ?: "")
                        } else {
                            sendAck(clientMessage, concurrentSession)
                        }
                    }
                } else {
                    sendErr(clientMessage, concurrentSession, "Operation not allowed, no permission.")
                }
            }

            is Pause -> {
                val pauseCommand = clientMessage.command
                // Do something with the pause command
            }

            is Resume -> {
                val resumeCommand = clientMessage.command
                // Do something with the resume command
            }

            is Ack -> {
                val userMessageStatuses = messageStatus.getMessageStatuses()[clientId]

                if (userMessageStatuses != null) {
                    userMessageStatuses[clientMessage.messageId] = MessageInfo(Instant.now(), true)
                    logger.info("[${clientMessage.messageId}] message acked")
                }
            }

            else -> {
                // Handle unknown command type
                logger.info("Received unknown command, {}", clientMessage)
            }
        }
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val concurrentSession = ConcurrentWebSocketSessionDecorator(
            session,
            5000,
            65536
        ) // TODO: Consider the given values or create specific function for this object instance creation
        sessionsStorage.addSession(concurrentSession)
        val timer = Timer()

        // Associate the session ID with the timer
        pingPongTimers[session.id] = timer

        schedulePingMessages(session, timer)

        val clientId = getClientIdFromSession(session)
        clientService.createClientIfNotExists(Client(clientId))
        logger.info("New connection from {}.", clientId)
    }

    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        // Retrieve the timer associated with the closed connection
        val timer = pingPongTimers.remove(session.id)
        timer?.cancel()

        recordDealer.removeSubscriptionsFromLocalMaps(sessionsStorage.getClientSession(session.id)!!)
        sessionsStorage.removeSession(session.id)

        val userId: String = getClientIdFromSession(session)
        // Remove the message statuses for the specific user
        messageStatus.getMessageStatuses().remove(userId)

        logger.info("Closed connection from {}.", userId)
    }

    override fun supportsPartialMessages(): Boolean {
        return false
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {}

    private fun getClientIdFromSession(session: WebSocketSession): String {
        return session.attributes[CLIENT_ID] as String
    }

    private fun sendAck(clientMessage: ClientMessage, session: WebSocketSession) {
        logger.info("[${clientMessage.messageId}] Sent ack")
        session.sendMessage(json(ClientMessage(clientMessage.messageId, Ack())))
    }

    private fun sendErr(clientMessage: ClientMessage, session: WebSocketSession, message: String) {
        logger.warn("[${clientMessage.messageId}] Sent Err (${message})")
        session.sendMessage(json(ClientMessage(clientMessage.messageId, Err(message))))
    }

    private fun schedulePingMessages(session: WebSocketSession, timer: Timer) {
        val pingMessage = PingMessage()

        val pingTask = object : TimerTask() {
            override fun run() {
                try {
                    session.sendMessage(pingMessage)
                } catch (e: IOException) {
                    // Handle any exceptions that occur while sending the ping message
                    e.printStackTrace()
                }
            }
        }

        // Schedule the ping task to run every 45 seconds
        timer.schedule(pingTask, 0, 45000)
    }
}
