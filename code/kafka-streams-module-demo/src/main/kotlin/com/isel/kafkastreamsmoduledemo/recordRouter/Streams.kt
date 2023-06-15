package com.isel.kafkastreamsmoduledemo.recordRouter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Component
class Streams(
    private val recordRouterUtils: RecordRouterUtils,
    private val kClientsStorage: StreamsStorage,
    private val rrTesting: RRTesting
) {

    companion object {
        private val SYSTEM_TOPIC = "SYSTEM_TOPIC"
        private val RECORD_ROUTER_SYSTEM_CONSUMER_ID = "RECORD_ROUTER_SYSTEM_CONSUMER"
        private val RECORD_ROUTER_KEYS_CONSUMER_ID = "RECORD_ROUTER_KEYS_CONSUMER"

    }
    private final val systemTopicConsumerExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private final val gatewayKeysTopicConsumerExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val mapper = jacksonObjectMapper()
    // Pair<Topic, Key>, List<GatewayTopic>
    private val gatewaysTopicKeys: ConcurrentHashMap<Pair<String, String>, List<String>> = ConcurrentHashMap<Pair<String, String>, List<String>>()
    // <Topic, List<GatewayTopic>> TODO: not yet used
    private val gatewaysFullTopics: ConcurrentHashMap<String, List<String>> = ConcurrentHashMap<String, List<String>>()
    private val gatewaysDetails: ConcurrentHashMap<String, GatewayDetails> = ConcurrentHashMap<String, GatewayDetails>()


    init {

        val systemTopic: String = "SYSTEM_TOPIC"
        val inputTopicA: String = "input-topic-a"
        val inputTopicB: String = "input-topic-b"

        rrTesting.deleteTopic(systemTopic)
        rrTesting.deleteTopic(inputTopicA)
        rrTesting.deleteTopic(inputTopicB)
        rrTesting.deleteTopic("gateway-01-clients-topic")
        rrTesting.deleteTopic("gateway-01-keys-topic")
        rrTesting.deleteTopic("gateway-02-clients-topic")
        rrTesting.deleteTopic("gateway-02-keys-topic")
        rrTesting.createTopic(systemTopic,3,3)
        rrTesting.createTopic(inputTopicA,3,3)
        rrTesting.createTopic(inputTopicB,3,3)
        rrTesting.createTopic("gateway-01-clients-topic",3,3)
        rrTesting.createTopic("gateway-01-keys-topic",3,3)
        rrTesting.createTopic("gateway-02-clients-topic",3,3)
        rrTesting.createTopic("gateway-02-keys-topic",3,3)

        listenSystemTopic()
    }


    final fun listenSystemTopic(): KafkaConsumer<String, String> {



        val consumer = KafkaConsumer<String, String>(recordRouterUtils.getSystemTopicConsumerProperties(
            RECORD_ROUTER_SYSTEM_CONSUMER_ID))

        consumer.subscribe(listOf(SYSTEM_TOPIC))

        systemTopicConsumerExecutor.submit {
            try {
                while (true) { // TODO: correct this later
                    consumer.poll(Duration.ofMillis(5000)).forEach { record ->
                        when (record.key()) {
                            "new-gateway-key-topic" -> { // TODO: What if stream exists already and alike
                                recordRouterUtils.printRed("listenSystemTopic: new-gateway-key-topic:\n ${record.value()}")
                                val systemGatewayKeyTopic: SystemGatewayKeyTopic =
                                    mapper.readValue<SystemGatewayKeyTopic>(record.value())
                                updateGatewayKeysTopicsSubscriptions(systemGatewayKeyTopic)
                                kClientsStorage.restartAllDefaultRoutingStreams()
                            }

                            else -> {
                                println("listenSystemTopic - record followed 'else' route.")
                            }
                        }
                    }
                }
            } catch (ex: Exception) {
                if (ex.cause is WakeupException) {
                    recordRouterUtils.printRed("listenSystemTopic -> consumer WakeupException.")
                }
            }
        }


        kClientsStorage.setSystemTopicConsumer(consumer) { listenSystemTopic() }
        return consumer
    }

    fun createGatewayKeysConsumer(): KafkaConsumer<String, String> {
        recordRouterUtils.printRed("createGatewayKeysConsumer")
        return KafkaConsumer<String, String>(recordRouterUtils.getGatewayKeysTopicConsumerProperties(
            RECORD_ROUTER_KEYS_CONSUMER_ID))
    }

    fun runGatewayKeysConsumerPoll(consumer: KafkaConsumer<String, String>) {
        recordRouterUtils.printRed("runGatewayKeysConsumerPoll")
        gatewayKeysTopicConsumerExecutor.submit {
            while(true) {
                recordRouterUtils.printRed("runGatewayKeysConsumerPoll before poll")
                consumer.poll(Duration.ofMillis(5000)).forEach { record ->
                    recordRouterUtils.printRed("runGatewayKeysConsumerPoll poll ENTRY")
                    val topic = record.key()
                    val keys: List<String> = mapper.readValue<List<String>>(record.value())
                    val newKeys: MutableList<String> = mutableListOf()
                    val toRemoveKeys: MutableList<String> = mutableListOf()
                    val gateway: GatewayDetails? = gatewaysDetails.values.find { gatewayDetails ->  gatewayDetails.systemGatewayKeyTopic.keysTopicName == record.topic()}
                    val clientTopicName: String? = gateway?.systemGatewayKeyTopic?.clientTopicName
                    if (clientTopicName == null) {
                        return@forEach
                    }
                    var newTopic: Boolean = false
                    recordRouterUtils.printRed("GatewayKeysConsumerPoll clientTopicName:[${clientTopicName}]")
                    gatewaysDetails.compute(gateway.gatewayId) { _, gatewayDetails ->
                        gatewayDetails?.topicsKeys?.compute(topic) { _, gatewayTopicKeys ->
                            if (gatewayTopicKeys == null) {
                                newTopic = true
                            }
                            val newGatewayTopicKeys = gatewayTopicKeys ?: listOf()
                            for (key: String in newGatewayTopicKeys) { // TODO: instead of two separate fors, try with only one, or two (one inside the other).
                                if (!keys.contains(key)) {
                                    toRemoveKeys.add(key)
                                }
                            }
                            for (key: String in keys) {
                                if (!newGatewayTopicKeys.contains(key)) {
                                    newKeys.add(key)
                                }
                            }

                            keys
                        }
                        gatewayDetails
                    }
                    for (key: String in newKeys) {
                        gatewaysTopicKeys.compute(Pair(topic, key)) { _, gatewayList ->
                            gatewayList?.plus(clientTopicName)
                                ?: listOf(clientTopicName)
                        }
                    }

                    for (key: String in toRemoveKeys) {
                        gatewaysTopicKeys.compute(Pair(topic, key)) { _, gatewayList ->
                            gatewayList?.minus(clientTopicName) ?: listOf()
                        }
                    }
                    recordRouterUtils.printRed("gatewaysTopicKeys topics+keys: ${gatewaysTopicKeys.keys().toList().toString()}")
                    recordRouterUtils.printRed("gatewaysTopicKeys values: ${gatewaysTopicKeys.values}")
                    if (newTopic) {
                        createTopicRecordRouterStream(topic, true)
                    }
                }
            }
        }
    }

    fun updateGatewayKeysTopicsSubscriptions(systemGatewayKeyTopic: SystemGatewayKeyTopic) {
        recordRouterUtils.printRed("updateGatewayKeysTopicsSubscriptions")

        var consumer = kClientsStorage.getGatewayKeysConsumer()
        if (consumer == null) {
            consumer = createGatewayKeysConsumer()
            kClientsStorage.setGatewayKeysConsumer(consumer) {createGatewayKeysConsumer()}
        } else {
            recordRouterUtils.printRed("updateGatewayKeysTopicsSubscriptions before wakeup")
            consumer.wakeup()
            consumer = createGatewayKeysConsumer() // TODO: Temporary
            kClientsStorage.setGatewayKeysConsumer(consumer) {createGatewayKeysConsumer()} // TODO: Temporary
        }
        recordRouterUtils.printRed("updateGatewayKeysTopicsSubscriptions after consumer==null if-else")


        gatewaysDetails.compute(systemGatewayKeyTopic.gatewayId) { gatewayId, details ->
            details?: GatewayDetails(gatewayId, systemGatewayKeyTopic, ConcurrentHashMap())
        }
        recordRouterUtils.printRed("updateGatewayKeysTopicsSubscriptions gatewayDetails size ${gatewaysDetails.size}")
        recordRouterUtils.printRed("updateGatewayKeysTopicsSubscriptions subscribe [${gatewaysDetails.values.toList().map { details -> details.systemGatewayKeyTopic.keysTopicName }}]")

        consumer.subscribe(gatewaysDetails.values.toList().map { details -> details.systemGatewayKeyTopic.keysTopicName })
        runGatewayKeysConsumerPoll(consumer)
    }

    private fun createTopicRecordRouterStream(topic: String, firstTime: Boolean): KafkaStreams {
        recordRouterUtils.printRed("createTopicRecordRouterStream: topic[${topic}] firstTime[${firstTime}]")
        recordRouterUtils.printRed("gatewayTopicKeys:\n ${gatewaysTopicKeys.elements().toList().toString()}")
        val builder = StreamsBuilder()

        val inputStream: KStream<String, String> =
            builder.stream(topic, Consumed.with(Serdes.String(), Serdes.String()))

        recordRouterUtils.printRed("createTopicRecordRouterStream gateways = ${gatewaysDetails.size}")
        for (gatewayEntry in gatewaysDetails) {
            recordRouterUtils.printRed("createTopicRecordRouterStream filter entry")
            inputStream.filter { key, _ -> isKeyForGateway(key, gatewayEntry.value.systemGatewayKeyTopic.clientTopicName, topic)}.to(gatewayEntry.value.systemGatewayKeyTopic.clientTopicName)
        }

        val stream = KafkaStreams(builder.build(),
            recordRouterUtils.topicRecordRouterStreamProperties(
                if(firstTime) "earliest" else "latest",
                "${topic}-topic-record-routing-stream"
            ))
        kClientsStorage.addDefaultRoutingStream("${topic}-topic-record-routing-stream", stream) { createTopicRecordRouterStream(topic, false) }

        stream.start()
        Runtime.getRuntime().addShutdownHook(Thread(stream::close))
        return stream

    }

    fun isKeyForGateway(key: String, gatewayClientTopicName: String, topic: String): Boolean {
        recordRouterUtils.printRed("isKeyForGateway: key[${key}]")
        return gatewaysTopicKeys[Pair(topic, key)]?.contains(gatewayClientTopicName)?: false
    }


}