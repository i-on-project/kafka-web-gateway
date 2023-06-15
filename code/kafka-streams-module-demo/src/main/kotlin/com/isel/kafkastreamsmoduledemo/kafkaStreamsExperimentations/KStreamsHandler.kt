package com.isel.kafkastreamsmoduledemo.kafkaStreamsExperimentations

import com.isel.kafkastreamsmoduledemo.utilsExperimentations.KafkaStreamsUtils
import com.isel.kafkastreamsmoduledemo.utilsExperimentations.KafkaStreamsUtils.Companion.KEY_FILTER_STORE
import com.isel.kafkastreamsmoduledemo.utilsExperimentations.TopicKeys
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.*
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.processor.RecordContext
import org.apache.kafka.streams.processor.api.Processor
import org.apache.kafka.streams.processor.api.ProcessorContext
import org.apache.kafka.streams.processor.api.Record
import org.apache.kafka.streams.state.KeyValueStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap


@Component
class KStreamsHandler(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String, // TODO: maybe change to db or kafka topic
    private val utils: KafkaStreamsUtils
) {

    private lateinit var plannerStream: KafkaStreams
    private var testGatewayKeys: ConcurrentHashMap<String, Array<TopicKeys>> = ConcurrentHashMap()
    companion object {


        // Processor that keeps the global store updated.
        private class GlobalStoresUpdater<K, V>(
            private val storeName: String
        ): Processor<K, V, Void, Void> { // not being used as the stores can only be accessed inside the processors.

            private var store: KeyValueStore<K, V>? = null

            override fun init(context: ProcessorContext<Void, Void>?) {
                store = context?.getStateStore(storeName)
            }

            override fun process(record: Record<K, V>?) {
                store!!.put(record!!.key(), record.value())
            }

        }
    }

    init {
        /*val builder = StreamsBuilder()

        builder.addGlobalStore(Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(KEY_FILTER_STORE), Serdes.String(), TopicKeysArraySerDe()),
            KEY_FILTER_TOPIC,
            Consumed.with(Serdes.String(), TopicKeysArraySerDe())){
            GlobalStoresUpdater<String, Bytes>(KEY_FILTER_STORE)
        }

        val inputStream = builder.stream(KEY_FILTER_TOPIC, Consumed.with(Serdes.String(), TopicKeysArraySerDe()))
        inputStream.peek{key, value ->
            plannerStream.store(KEY_FILTER_STORE, QueryableStoreTypes.keyValueStore())
        }
        plannerStream = KafkaStreams(builder.build(), getStreamDefaultProperties())*/

        // TODO: delete after use -> these mock values
        testGatewayKeys.put("gateway01", arrayOf(
            TopicKeys("topicA", arrayOf(
                1L, 0L
            ))
        ))
        testGatewayKeys.put("gateway02", arrayOf(
            TopicKeys("topicA", arrayOf(
                2L, 0L
            ))
        ))

        //loggerConsumer(listOf("gateway01", "gateway02", "gateway03"))
    }

    fun insertToGatewayKeys(gateway: String) {
        testGatewayKeys.put("gateway03", arrayOf(
            TopicKeys("topicA", arrayOf(
                3L, 0L
            ))
        ))
    }

    fun addNewKeyToGateway01() {
        testGatewayKeys.put("gateway01", arrayOf(
            TopicKeys("topicA", arrayOf(
                1L, 0L, 4L
            ))
        ))
    }

    class IsNewTopicProcessor: Processor<String, Array<TopicKeys>, String, Array<TopicKeys>> {

        var store: KeyValueStore<String, Array<TopicKeys>>? = null
        override fun init(context: ProcessorContext<String, Array<TopicKeys>>?) {
            this.store = context?.getStateStore(KEY_FILTER_STORE)

        }

        // See if received record
        override fun process(record: Record<String, Array<TopicKeys>>?) {

        }

    }


    fun newStreamForTopic(topic: String, streamId: String) {
        println("startGatewayFiltersStream function beginning")

        val builder = StreamsBuilder()
        val inputStream: KStream<Long, String> = builder.stream(topic, Consumed.with(Serdes.Long(), Serdes.String()))

        // TODO: hardcoded the position 0 as a fix should be thought of to send the record to more than one topic
        /* try {
            inputStream.to { key, value, recordContext ->
                gatewayKeyFilter(
                    key,
                    value,
                    topic
                ).find { topic -> true }
            }
        } catch (ex: NullPointerException) {
            println("Possibly another orphan record")
        } */
        /* inputStream.peek{key, value -> // TODO: test this option
            for (entry in testGatewayKeys) {
                for (topicKeysArray in entry.value) {
                    if (topicKeysArray.keys.contains(key)) {
                        inputStream.selectKey { key, value ->  key}.to(topicKeysArray.topic)

                    }
                }
            }
        } */

        /* val branched: BranchedKStream<Long, String> = inputStream.split()
        for (gatewayEntry in testGatewayKeys) {
            println("for gateway entry [${gatewayEntry.key}]")
            branched.branch(
                { k: Long, v: String -> isKeyForGateway(k, gatewayEntry.key, topic) },
                Branched.withConsumer { ks -> ks.to(gatewayEntry.key)}
            )
        } */

        for (gatewayEntry in testGatewayKeys) {
            inputStream.filter {key, value -> isKeyForGateway(key, gatewayEntry.key, topic)}.to(gatewayEntry.key)
        }

        val stream = KafkaStreams(builder.build(), utils.getStreamDefaultProperties())
        utils.streamsMap.put(topic ,stream)

        stream.cleanUp()
        stream.start()

        Runtime.getRuntime().addShutdownHook(Thread(stream::close))
        println("startGatewayFiltersStream function ending")
    }



    private fun isKeyForGateway(key: Long, gateway: String, topic: String): Boolean {
        println("STARTING isKeyForGateway")
        if (testGatewayKeys[gateway].isNullOrEmpty()) {
            return false
        }
        for (topicKeys in testGatewayKeys[gateway]!!) {
            if (topicKeys.topic == topic) {
                for (topicKey in topicKeys.keys) {
                    if (topicKey == key) {
                        println("isKeyForGateway returning true key[$key] topic[$topic] gateway[$gateway]")
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun gatewayKeyFilter(key: Long, value: String, inputTopic: String): ArrayList<String> {
        val outputTopics: ArrayList<String> = ArrayList()
        println("started filtering a value")

        for (gatewayEntry in testGatewayKeys) {
            println("searching for ${gatewayEntry.key}")
            val gatewayTopicKeys = gatewayEntry.value.find { topicKeys: TopicKeys ->  topicKeys.topic == inputTopic}
            for (gatewayTopicKey in gatewayTopicKeys?.keys!!) {
                if (gatewayTopicKey == key) {
                    println("found key:[$key] for ${gatewayEntry.key}")
                    outputTopics.add(gatewayEntry.key)
                    break
                }
            }
        }
        println("nr of output topics is ${outputTopics.count()}")
        return outputTopics
    }

    fun startStreaming(id: String): String {
        println("startStreaming id: [$id]")

        if (utils.streamsMap.contains(id)) {
            utils.streamsMap.get(id)?.close()
            utils.streamsMap.remove(id)
            println("startStreaming id: [$id] -> stream already existed, re-writing..")
        }

        val props = Properties()
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-app-id-1-example")
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1)
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Long().javaClass)
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().javaClass)

        val builder = StreamsBuilder()
        val topics = listOf("input-topic-A", "input-topic-B")
        val inputStream: KStream<Long, String> = builder.stream(topics, Consumed.with(Serdes.Long(), Serdes.String()))

        /*inputStream.peek { key, value ->
            if ((key % 2).toInt() == 0) {
                // Process record with even key
                println("Processing record with even key: [$key] and value[$value]")
                inputStream.selectKey { k, v -> key }.to("even", Produced.with(Serdes.Long(), Serdes.String()))
            } else {
                // Process record with odd key
                println("Processing record with odd key: [$key] and value[$value]")
                inputStream.selectKey { k, v -> key }.to("uneven", Produced.with(Serdes.Long(), Serdes.String()))
            }
        }*/

        //inputStream.to { key, value, recordContext -> evenFilter(key, value, recordContext) }
        inputStream.to("even")


        val topology = builder.build()
        val streams = KafkaStreams(topology, props)
        utils.streamsMap.put(id, streams)

        streams.cleanUp()
        streams.start()

        return topology.describe().toString()
    }

    private fun evenFilter(key: Long, value: String, recordContext: RecordContext): String {
        return if ((key % 2).toInt() == 0) {
            println("[${System.currentTimeMillis()}] - Processing record with even key: [$key] and value[$value] from topic:[${recordContext.topic()}] and timestamp [${recordContext.timestamp()}]")
            "even"
        } else {
            println("[${System.currentTimeMillis()}] - Processing record with uneven key: [$key] and value[$value] from topic:[${recordContext.topic()}] and timestamp [${recordContext.timestamp()}]")
            "uneven"
        }
    }

    final fun loggerConsumer(topics: List<String> = listOf("even", "uneven")) {
        utils.loggerConsumer(topics)
    }

    fun closeConsumerLogger() {
        utils.streamsMap.remove("logger-stream")
    }

    fun loggerStream() {
        if (utils.streamsMap.contains("logger-stream")) {
            utils.streamsMap.get("logger-stream")?.close()
            utils.streamsMap.remove("logger-stream")
            return
        }


        val props = Properties()
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-app-id-1-logger")
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Long().javaClass)
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().javaClass)

        val builder = StreamsBuilder()
        val evenTopic = "even"
        val unevenTopic = "uneven"
        val evenInputStream: KStream<Long, String> = builder.stream(evenTopic, Consumed.with(Serdes.Long(), Serdes.String()))
        val unevenInputStream: KStream<Long, String> = builder.stream(unevenTopic, Consumed.with(Serdes.Long(), Serdes.String()))

        evenInputStream.peek { key, value ->
            println("[${System.currentTimeMillis()}] - Even topic message received with key: [$key]")
        }
        unevenInputStream.peek { key, value ->
            println("[${System.currentTimeMillis()}] - Uneven topic message received with key: [$key]")
        }

        val topology = builder.build()
        val streams = KafkaStreams(topology, props)
        utils.streamsMap.put("logger-stream", streams)

        streams.cleanUp()
        streams.start()
    }

    fun closeStream(id: String): String {
        println("closeStream id: [$id]")
        return if(utils.streamsMap.remove(id) != null) "removed" else "no key"
    }


}