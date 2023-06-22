package com.isel.ps.gateway.kafka

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.isel.ps.gateway.config.GatewayConfig
import com.isel.ps.gateway.model.ClientMessage
import com.isel.ps.gateway.model.Message
import com.isel.ps.gateway.model.MessageStatus
import com.isel.ps.gateway.model.Subscription
import com.isel.ps.gateway.utils.*
import com.isel.ps.gateway.websocket.ClientSession
import com.isel.ps.gateway.websocket.GatewayWebsocketHandler.Companion.json
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

// TODO: Rule: Client interactions with this class only on at a time per client.
@Component
class RecordDealer(
    private val kafkaClientsUtils: KafkaClientsUtils,
    private val gatewayConfig: GatewayConfig,
    private val producer: KafkaProducer<String, String>,
    private val messageStatus: MessageStatus
) {
    // TODO: Think about multiple consumers (probably same group id) per gateway
    private val consumer: KafkaConsumer<String, String>

    // Holds subscriptions that are limited by keys. Pair of topic-key as hashmap key.
    private val keys: ConcurrentHashMap<Pair<String, String>, List<ClientSession>> =
        ConcurrentHashMap<Pair<String, String>, List<ClientSession>>()

    // Holds subscriptions that are NOT limited by keys.
    private val fullTopics: ConcurrentHashMap<String, List<ClientSession>> =
        ConcurrentHashMap<String, List<ClientSession>>()
    private final val consumerExecutor: ExecutorService =
        Executors.newSingleThreadExecutor() //TODO: temporary way to use particular thread
    private final val messageExecutor: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor() //TODO: temporary way to use particular thread

    // TODO: Debate this value
    private var consumerPollMillis: Long = 10000L
    private val mapper = jacksonObjectMapper()

    private val logger: Logger = LoggerFactory.getLogger(RecordDealer::class.java)

    init {
        val gatewayInstance = gatewayConfig.getGateway()
        consumer =
            KafkaConsumer(kafkaClientsUtils.getConsumerDefaultProperties(gatewayIdLongToConsumerIdString(gatewayInstance.gatewayId)))
        consumer.subscribe(listOf(gatewayInstance.topicClients))
        consumerExecutor.submit {
            // TODO: consider using consumer.wakeup and/or global boolean maybe
            while (true) {
                val records: ConsumerRecords<String, String>? = consumer.poll(Duration.ofMillis(consumerPollMillis))
                records?.forEach { record ->
                    dispatch(record)
                }
            }
        }

    }

    private final fun gatewayIdLongToConsumerIdString(gatewayId: Long): String {
        return "gateway-${gatewayId}"
    }

    /**
     * Search the keys map in search of all the clients subscribing the key from the topic of received record.
     * Then, send the value of said topic to websocket handler with intent to send to client.
     */
    private fun dispatch(record: ConsumerRecord<String, String>) {
        val clients: MutableList<ClientSession> = mutableListOf()

        val originalTopic = getOriginalTopic(record)

        keys[Pair(originalTopic, record.key())]?.let { clients.addAll(it.toList()) }
        fullTopics[originalTopic]?.let { clients.addAll(it.toList()) }

        logger.info("RecordDealer dispatch() clients found -> [${clients.size}]")

        clients.forEach { clientSession ->
            val messageId = UUID.randomUUID().toString()

            clientSession.session.sendMessage(
                messageId,
                json(
                    ClientMessage(
                        messageId,
                        Message(
                            originalTopic,
                            record.partition(),
                            record.key(),
                            record.value(),
                            record.timestamp(),
                            record.offset()
                        )
                    )
                ),
                messageStatus.getMessageStatuses(),
                messageExecutor,
                3
            )
        }
    }

    private fun getOriginalTopic(record: ConsumerRecord<String, String>) =
        String(record.headers().lastHeader("original_topic").value())

    fun addSubscription(clientSession: ClientSession, subscription: Subscription): AddSubscriptionResult {
        return when (val res = addSubscriptionToLocalMaps(clientSession, subscription)) {
            is Result.Success -> when (res.value) {
                is AddSubscriptionSuccess.Added -> {
                    updateRequestedKeysTopic(subscription.topic)
                    Result.Success(AddSubscriptionSuccess.Added)
                }

                else -> Result.Error(AddSubscriptionError.Unknown)
            }

            else -> Result.Error(AddSubscriptionError.Unknown)
        }

    }

    private fun updateRequestedKeysTopic(topic: String) {
        val record = ProducerRecord(
            "gateway-${gatewayConfig.getGateway().gatewayId}-keys",
            topic,
            mapper.writeValueAsString(getAllTopicKeys(topic))
        )
        producer.send(record)
    }

    private fun getAllTopicKeys(topic: String): List<String> {
        return keys.keys().toList().filter { it.first == topic }.map { it.second }
    }

    private fun addSubscriptionToLocalMaps(
        clientSession: ClientSession,
        subscription: Subscription
    ): AddSubscriptionResult {

        // If subscription topic key is null
        if (subscription.key == null) {
            fullTopics.compute(subscription.topic) { _, topicKeys ->
                topicKeys?.plus(clientSession)?.distinct() ?: listOf(clientSession)
            }
        } else {
            keys.compute(Pair(subscription.topic, subscription.key)) { _, topicKeys ->
                topicKeys?.plus(clientSession)?.distinct() ?: listOf(clientSession)
            }
        }
        return Result.Success(AddSubscriptionSuccess.Added)
    }

    fun removeSubscriptionsFromLocalMaps(clientSession: ClientSession) {
        // Remove the clientSession from subscription lists limited by keys
        keys.forEach { (topicKey, clientSessions) ->
            keys[topicKey] = clientSessions.filterNot { it == clientSession }
        }

        // Remove the clientSession from subscription lists not limited by keys
        fullTopics.forEach { (topic, clientSessions) ->
            fullTopics[topic] = clientSessions.filterNot { it == clientSession }
        }
    }
}