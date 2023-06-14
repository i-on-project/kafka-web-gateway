package com.isel.ps.gateway.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.isel.ps.gateway.db.SubscriptionRepository
import com.isel.ps.gateway.kafka.RecordDealer
import com.isel.ps.gateway.model.*
import com.isel.ps.gateway.utils.SendTask
import com.isel.ps.gateway.websocket.ClientAuthenticationInterceptor.Companion.CLIENT_ID
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.socket.*
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator
import java.util.*
import java.util.concurrent.*

@Component
class GatewayWebsocketHandler(
    private val subscriptionRepository: SubscriptionRepository,
    private val sessionsStorage: ClientSessions,
    private val kafkaProducer: KafkaProducer<String, String>,
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String,
    private val recordDealer: RecordDealer
) : WebSocketHandler {

    private val logger: Logger = LoggerFactory.getLogger(GatewayWebsocketHandler::class.java)
    private var executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    // val subscriptions: MutableMap<String, List<TopicType>> = mutableMapOf()

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
        clientMessage.messageId = UUID.randomUUID().toString()
        println("handleMessage: $clientMessage")

        when (clientMessage.command) {
            is Subscribe -> {
                val subscribeCommand = clientMessage.command
                subscribeCommand.topics.forEach { topic ->
                    val subscription: Subscription = Subscription((Math.random()*10000).toInt(), session.id, topic.topic, topic.key) //TODO: debate how to input the values..
                    recordDealer.addSubscription(sessionsStorage.getClientSession(session.id)!!, subscription)
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

            is Publish -> { //TODO: Needs filtering/confirmation for sending records to topics
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
                    userMessageStatuses[clientMessage.messageId] = true
                    logger.info("[${clientMessage.messageId}] message acked")
                }
            }

            else -> {
                // Handle unknown command type
                logger.info("Received unknown command, {}", clientMessage)
            }
        }

        // sendAck(clientMessage, session)
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val concurrentSession = ConcurrentWebSocketSessionDecorator(session, 5000, 65536) // TODO: Consider the given values or create specific function for this object instance creation
        sessionsStorage.addSession(concurrentSession)
        logger.info("New connection from {}.", getUserIdFromSession(session))
    }

    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        val userId: String = getUserIdFromSession(session)
        sessionsStorage.removeSession(session.id)

        // Remove the message statuses for the specific user
        messageStatuses.remove(userId)
        logger.info("Closed connection from {}.", userId)
    }

    override fun supportsPartialMessages(): Boolean {
        return false
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {}

    private fun getUserIdFromSession(session: WebSocketSession): String {
        return session.attributes[CLIENT_ID] as String
    }

    private fun sendAck(clientMessage: ClientMessage, session: WebSocketSession) {
        session.sendMessage(json(ClientMessage(clientMessage.messageId, Ack())))
    }

    private fun json(obj: Any) = TextMessage(objectMapper.writeValueAsString(obj))

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

    fun ConcurrentWebSocketSessionDecorator.sendMessage(textMessage: WebSocketMessage<*>, retries: Int = 3) {
        val session = this
        val userId = getUserIdFromSession(this)

        val messageId = UUID.randomUUID().toString()

        // Create a nested map for each user if it doesn't exist
        messageStatuses.putIfAbsent(userId, ConcurrentHashMap())

        messageStatuses[userId]!![messageId] = false

        var remainingRetries = retries

        logger.info("userId: $userId")
        val sendTask = SendTask(
            messageStatuses,
            userId,
            messageId,
            remainingRetries,
            session,
            textMessage,
            executor,
            logger
        )

        executor.schedule(sendTask, 10000L, TimeUnit.MILLISECONDS)
    }

    fun sendToClient(session: ConcurrentWebSocketSessionDecorator, textMessage: WebSocketMessage<*>) {
        session.sendMessage(textMessage)
    }
}
