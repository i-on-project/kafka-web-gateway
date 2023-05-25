package com.isel.ps.gateway.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.isel.ps.gateway.model.CommandDeserializer
import com.isel.ps.gateway.model.GatewayTypes.*
import com.isel.ps.gateway.model.TopicTypeDeserializer
import com.isel.ps.gateway.model.USER_ID
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.socket.*
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


class MyWebSocketHandler(
    private val kafkaProducer: KafkaProducer<String, String>,
    private val kafkaConsumer: KafkaConsumer<String, String>,
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String
) : WebSocketHandler {

    private val LOGGER: Logger = LoggerFactory.getLogger(MyWebSocketHandler::class.java)
    var executor: Timer = Timer()
    val subscriptions: MutableMap<String, List<TopicType>> = mutableMapOf()

    private val objectMapper = ObjectMapper().also {
        val module = SimpleModule()
        module.addDeserializer(Command::class.java, CommandDeserializer())
        module.addDeserializer(TopicType::class.java, TopicTypeDeserializer())
        it.registerModule(module)
    }

    private val messageStatuses: ConcurrentMap<String, ConcurrentMap<String, Boolean>> = ConcurrentHashMap()

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        val concurrentSession = ConcurrentWebSocketSessionDecorator(session, 5000, 65536)
        val payload = message.payload as String
        val clientMessage = objectMapper.readValue<ClientMessage>(payload)
        println("handleMessage: $clientMessage")

        when (clientMessage.command) {
            is Subscribe -> {
                val subscribeCommand = clientMessage.command
                subscribeCommand.topics.forEach { topic ->
                    /*
                    val props = Properties()
                    props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
                    props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
                    props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
                    // props[ConsumerConfig.GROUP_ID_CONFIG] = "my-group"
                    subscriptions[topic.topic] = kafkaConsumer.subscribe(props)

                     */
                }
            }

            is Consume -> {
                val consumeCommand = clientMessage.command
                concurrentSession.sendMessage(TextMessage("Please consume this message"), 3)
            }

            is Publish -> {
                val publishCommand = clientMessage.command
                val producerRecord = ProducerRecord(publishCommand.topic, publishCommand.key, publishCommand.value)
                kafkaProducer.send(producerRecord) { _, err ->
                    if (err != null) {
                        concurrentSession.sendMessage(json(Err(err.message)))
                    }

                    sendAck(clientMessage, concurrentSession)
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
                val userMessageStatuses = messageStatuses[getUserIdFromSession(session)]

                if (userMessageStatuses != null) {
                    userMessageStatuses[clientMessage.messageID] = true
                    LOGGER.info("[${clientMessage.messageID}] message acked")
                }
            }

            else -> {
                // Handle unknown command type
                LOGGER.info("Received unknown command, {}", clientMessage)
            }
        }

        sendAck(clientMessage, session);
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        LOGGER.info("New connection from {}.", getUserIdFromSession(session))
    }

    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        val userId: String = getUserIdFromSession(session)

        // Remove the message statuses for the specific user
        messageStatuses.remove(userId)
        LOGGER.info("Closed connection from {}.", userId)
    }

    override fun supportsPartialMessages(): Boolean {
        return false
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {}

    private fun getUserIdFromSession(session: WebSocketSession): String {
        return session.attributes[USER_ID] as String
    }

    private fun sendAck(clientMessage: ClientMessage, session: WebSocketSession) {
        session.sendMessage(json(ClientMessage(clientMessage.messageID, Ack())))
    }

    private fun json(obj: Any) = TextMessage(objectMapper.writeValueAsString(obj))

    private fun ConcurrentWebSocketSessionDecorator.sendMessage(textMessage: WebSocketMessage<*>, retries: Int = 3) {
        val session = this
        val userId = getUserIdFromSession(this)

        val messageId = UUID.randomUUID().toString()

        // Create a nested map for each user if it doesn't exist
        messageStatuses.putIfAbsent(userId, ConcurrentHashMap())

        messageStatuses[userId]!![messageId] = false

        var remainingRetries = retries

        LOGGER.info("userId: $userId")
        val sendTask = SendTask(
            messageStatuses,
            userId,
            messageId,
            remainingRetries,
            session,
            textMessage,
            executor,
            LOGGER
        )

        executor.schedule(sendTask, 10000L)
    }

    private fun Timer.schedule(delay: Long, period: Long = 0, action: TimerTask.() -> Unit): TimerTask {
        val task = object : TimerTask() {
            override fun run() {
                action()
            }
        }
        if (period > 0) {
            this.scheduleAtFixedRate(task, delay, period)
        } else {
            this.schedule(task, delay)
        }
        return task
    }
}

class SendTask(
    private val messageStatuses: Map<String, Map<String, Boolean>>,
    private val userId: String,
    private val messageId: String,
    private val remainingRetries: Int,
    private val session: WebSocketSession,
    private val textMessage: WebSocketMessage<*>,
    private val executor: Timer,
    private val logger: Logger
) : TimerTask() {
    override fun run() {
        logger.info("messageStatuses[userId]?.get(messageId): ${messageStatuses[userId]?.get(messageId)}")
        logger.info("userId: $userId messageId: $messageId")
        if (messageStatuses[userId]?.get(messageId) != true && remainingRetries > 0) {
            session.sendMessage(textMessage)
            rescheduleTask(remainingRetries - 1)
        } else {
            // Handle when the message was acknowledged or retries are exhausted
            if (remainingRetries == 0) {
                // Retry limit reached
                logger.info("Message retries exhausted")
            } else {
                // Message acknowledged
                logger.info("Message acknowledged")
            }
        }
    }

    private fun rescheduleTask(retries: Int) {
        val newTask = SendTask(
            messageStatuses,
            userId,
            messageId,
            retries,
            session,
            textMessage,
            executor,
            logger
        )
        executor.schedule(newTask, 10000L)
    }
}