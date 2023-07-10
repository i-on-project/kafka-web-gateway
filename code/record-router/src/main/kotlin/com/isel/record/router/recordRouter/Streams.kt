package com.isel.record.router.recordRouter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.processor.api.Processor
import org.apache.kafka.streams.processor.api.ProcessorContext
import org.apache.kafka.streams.processor.api.ProcessorSupplier
import org.apache.kafka.streams.processor.api.Record
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.thread


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
    private val gatewaysDetails: ConcurrentHashMap<String, HubDetails> = ConcurrentHashMap<String, HubDetails>()


    init {

        //rrTesting.testInitializer()

        //stateShower()
        listenSystemTopic()
    }

    final fun stateShower() {
        thread {
            while (true){
                recordRouterUtils.printGreen("********** gatewaysTopicKeys **********")
                for (entry in gatewaysTopicKeys) {
                    recordRouterUtils.printGreen("topic[${entry.key.first}] key[${entry.key.second}] gateway-client-topic: ${entry.value.toString()}")
                }
                println("-------------------------")
                println()

                recordRouterUtils.printGreen("********** gatewaysFullTopics **********")
                for (entry in gatewaysFullTopics) {
                    recordRouterUtils.printGreen("topic[${entry.key}] gateway-client-topic: ${entry.value}")
                }
                println("-------------------------")
                println()

                recordRouterUtils.printGreen("********** gatewaysDetails **********")
                for (entry in gatewaysDetails) {
                    recordRouterUtils.printGreen("gateway[${entry.key}] gatewayId[${entry.value.hubId}]")
                }
                println("-------------------------")
                println()

                Thread.sleep(1000)
            }
        }

    }


    final fun listenSystemTopic(): KafkaConsumer<String, String> {



        val consumer = KafkaConsumer<String, String>(recordRouterUtils.getSystemTopicConsumerProperties(
            RECORD_ROUTER_SYSTEM_CONSUMER_ID + Math.random() * 10000
        ))

        consumer.subscribe(listOf(SYSTEM_TOPIC))

        systemTopicConsumerExecutor.submit {
            try {
                while (true) { // TODO: correct this later
                    consumer.poll(Duration.ofMillis(5000)).forEach { record ->
                        when (record.key()) {
                            "new-gateway" -> { // TODO: What if stream exists already and alike
                                recordRouterUtils.printRed("listenSystemTopic: new-gateway:\n ${record.value()}")
                                val systemHub: SystemHub =
                                    mapper.readValue<SystemHub>(record.value())
                                    updateGatewayKeysTopicsSubscriptions(systemHub)
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
                    consumer.close()
                }
            }
        }


        kClientsStorage.setSystemTopicConsumer(consumer) { listenSystemTopic() }
        return consumer
    }

    fun createGatewayKeysConsumer(): KafkaConsumer<String, String> {
        recordRouterUtils.printRed("createGatewayKeysConsumer")
        return KafkaConsumer<String, String>(recordRouterUtils.getGatewayKeysTopicConsumerProperties(
            RECORD_ROUTER_KEYS_CONSUMER_ID + Math.random() * 10000
        ))
    }

    fun runGatewayKeysConsumerPoll(consumer: KafkaConsumer<String, String>) {
        recordRouterUtils.printRed("runGatewayKeysConsumerPoll")
        gatewayKeysTopicConsumerExecutor.submit {
            try {
                while(true) {
                    recordRouterUtils.printRed("runGatewayKeysConsumerPoll before poll")
                    consumer.poll(Duration.ofMillis(5000)).forEach { record ->
                        recordRouterUtils.printRed("runGatewayKeysConsumerPoll poll ENTRY")
                        val topic = record.key()
                        var keys: HubTopicKeys = mapper.readValue<HubTopicKeys>(record.value()) // TODO: Think about missing protections cases
                        if (keys.keys == null) {
                            keys = HubTopicKeys(listOf(), keys.allKeys)
                        }
                        val newKeys: MutableList<String> = mutableListOf()
                        val toRemoveKeys: MutableList<String> = mutableListOf()
                        val gateway: HubDetails? =
                            gatewaysDetails.values.find { gatewayDetails -> gatewayDetails.systemHub.keysTopicName == record.topic() }
                        val clientTopicName: String? = gateway?.systemHub?.clientsTopicName
                        if (clientTopicName == null) {
                            return@forEach
                        }
                        var isNewTopic = BooleanObj(false)
                        recordRouterUtils.printRed("GatewayKeysConsumerPoll clientTopicName:[${clientTopicName}]")

                        updateNecessaryTopicKeys(keys, newKeys, toRemoveKeys, gateway, topic, clientTopicName, isNewTopic)

                        recordRouterUtils.printRed(
                            "gatewaysTopicKeys topics+keys: ${
                                gatewaysTopicKeys.keys().toList().toString()
                            }"
                        )
                        recordRouterUtils.printRed("gatewaysTopicKeys values: ${gatewaysTopicKeys.values}")
                        if (isNewTopic.value) {
                            recordRouterUtils.printRed("GatewayKeysConsumerPoll isNewTopic")
                            createTopicRecordRouterStream(topic, true)
                        }
                    }
                }
            } catch(ex: WakeupException) {
                recordRouterUtils.printRed("---------- Woke up poll ----------")
                consumer.close()
            }
        }
    }

    private fun updateNecessaryTopicKeys(
        keys: HubTopicKeys,
        newKeys: MutableList<String>,
        toRemoveKeys: MutableList<String>,
        gateway: HubDetails,
        topic: String,
        clientTopicName: String,
        isNewTopic: BooleanObj
    ) {
        recordRouterUtils.printRed("updateNecessaryTopicKeys")
        gatewaysDetails.compute(gateway.hubId) { _, gatewayDetails ->
            gatewayDetails?.topicsKeys?.compute(topic) { _, gatewayTopicKeys ->
                if (gatewayTopicKeys == null) {
                    isNewTopic.value = true
                }
                val newGatewayTopicKeys = gatewayTopicKeys ?: listOf()
                for (key: String in newGatewayTopicKeys) { // TODO: instead of two separate fors, try with only one, or two (one inside the other).
                    if (!keys.keys!!.contains(key)) {
                        toRemoveKeys.add(key)
                    }
                }
                for (key: String in keys.keys!!) {
                    if (!newGatewayTopicKeys.contains(key)) {
                        newKeys.add(key)
                    }
                }



                keys.keys.map { it }// Returning copy
            }

            while (true){
                if (keys.keys!!.isEmpty()) {
                    val fullTopicsValue = gatewayDetails?.fullTopics!!.get()
                    if (gatewayDetails.fullTopics.compareAndSet(fullTopicsValue, fullTopicsValue.plus(topic))) {
                        break
                    }
                } else {
                    break
                }
            }
            if (keys.allKeys == true) {
                recordRouterUtils.printRed("(keys.allKeys == true)")
                gatewaysFullTopics.compute(topic) { _, gatewayClientTopics ->
                    gatewayClientTopics?.plus(clientTopicName) ?: listOf(clientTopicName)

                }
            } else {
                recordRouterUtils.printRed("(keys.allKeys == false or null)")
                for (key: String in newKeys) {
                    gatewaysTopicKeys.compute(Pair(topic, key)) { _, gatewayList ->
                        gatewayList?.plus(clientTopicName)
                            ?: listOf(clientTopicName)
                    }
                }
            }

            for (key: String in toRemoveKeys) {
                gatewaysTopicKeys.compute(Pair(topic, key)) { _, gatewayList ->
                    gatewayList?.minus(clientTopicName) ?: listOf()
                }
            }
            gatewayDetails
        }

    }

    fun updateGatewayKeysTopicsSubscriptions(systemHub: SystemHub) {
        recordRouterUtils.printRed("updateGatewayKeysTopicsSubscriptions")

        var consumer = kClientsStorage.getGatewayKeysConsumer()
        if (consumer == null) {
            recordRouterUtils.printRed("updateGatewayKeysTopicsSubscriptions consumer is null")
            consumer = createGatewayKeysConsumer()
            kClientsStorage.setGatewayKeysConsumer(consumer) {createGatewayKeysConsumer()}
        } else {
            recordRouterUtils.printRed("updateGatewayKeysTopicsSubscriptions before wakeup")
            try {
                consumer.wakeup()
            } catch (ex: Exception) {
                recordRouterUtils.printRed("Exception in wakeup or close with exception: ${ex.message}")
                throw ex
            }
            consumer = createGatewayKeysConsumer() // TODO: Temporary
            kClientsStorage.setGatewayKeysConsumer(consumer) {createGatewayKeysConsumer()} // TODO: Temporary
        }
        recordRouterUtils.printRed("updateGatewayKeysTopicsSubscriptions after consumer==null if-else")


        gatewaysDetails.compute(systemHub.hubId) { gatewayId, details ->
            details?: HubDetails(gatewayId, systemHub, ConcurrentHashMap())
        }
        recordRouterUtils.printRed("updateGatewayKeysTopicsSubscriptions gatewayDetails size ${gatewaysDetails.size}")
        recordRouterUtils.printRed("updateGatewayKeysTopicsSubscriptions subscribe [${gatewaysDetails.values.toList().map { details -> details.systemHub.keysTopicName }}]")

        consumer.subscribe(gatewaysDetails.values.toList().map { details -> details.systemHub.keysTopicName })
        runGatewayKeysConsumerPoll(consumer)
    }

    private fun createTopicRecordRouterStream(topic: String, firstTime: Boolean): KafkaStreams { //TODO: change to example of non deprecated process (store example)
        recordRouterUtils.printRed("createTopicRecordRouterStream: topic[${topic}] firstTime[${firstTime}]")
        recordRouterUtils.printRed("gatewayTopicKeys:\n ${gatewaysTopicKeys.elements().toList().toString()}")
        val topology = Topology()
        topology.addSource(if (firstTime) Topology.AutoOffsetReset.EARLIEST else Topology.AutoOffsetReset.LATEST, "${topic}-source", Serdes.String().deserializer(), Serdes.String().deserializer(), topic)
        topology.addProcessor("${topic}-processor", ProcessorSupplier { MyProcessor(topic, gatewaysTopicKeys, gatewaysFullTopics, recordRouterUtils) }, "${topic}-source")

        recordRouterUtils.printRed("createTopicRecordRouterStream gateways = ${gatewaysDetails.size}")
        for (gatewayEntry in gatewaysDetails) {
            recordRouterUtils.printRed("createTopicRecordRouterStream filter entry")
            topology.addSink("${gatewayEntry.value.systemHub.clientsTopicName}-sink", gatewayEntry.value.systemHub.clientsTopicName, "${topic}-processor")
        }

        val stream = KafkaStreams(topology,
            recordRouterUtils.topicRecordRouterStreamProperties(
                if(firstTime) "earliest" else "latest",
                "${topic}-topic-record-routing-stream"
            ))
        kClientsStorage.addDefaultRoutingStream("${topic}-topic-record-routing-stream", stream) { createTopicRecordRouterStream(topic, false) }

        stream.start()
        Runtime.getRuntime().addShutdownHook(Thread(stream::close))
        return stream

    }


    class MyProcessor(
        private val topic: String,
        private val keys: ConcurrentHashMap<Pair<String, String>, List<String>>,
        private val fullTopics: ConcurrentHashMap<String, List<String>>,
        private val utils: RecordRouterUtils
    ) : Processor<String, String, String, String> {
        private var context: ProcessorContext<String, String>? = null
        override fun init(context: ProcessorContext<String, String>?) {
            this.context = context
        }

        override fun process(record: Record<String, String>?) {
            if (record == null || context == null) {
                utils.printWithPurpleBackground("\n\nMyProcessor record[${record} context[${context}]]\n\n")
                return
            }
            record.headers()?.add("origin-topic", context?.recordMetadata()?.get()?.topic()?.toByteArray())

            val outputTopics: MutableSet<String> = mutableSetOf()
            fullTopics[topic]?.let { outputTopics.addAll(it.toMutableSet()) }
            keys[Pair(topic, record.key())]?.let { outputTopics.addAll(it.toMutableSet()) }
            for (outputTopic in outputTopics) {
                utils.printWithPurpleBackground("Processor.process: inputTopic[${topic}] outputTopic[${outputTopic}] key[${record.key()}] value[${record.value()}]")
                context!!.forward(record, "${outputTopic}-sink")
            }

        }

    }


    fun isKeyForGateway(key: String, gatewayClientTopicName: String, topic: String): Boolean {
        recordRouterUtils.printRed("isKeyForGateway: key[${key}]")
        return gatewaysTopicKeys[Pair(topic, key)]?.contains(gatewayClientTopicName) == true ||
                        gatewaysFullTopics[topic]?.contains(gatewayClientTopicName) == true
    }


}