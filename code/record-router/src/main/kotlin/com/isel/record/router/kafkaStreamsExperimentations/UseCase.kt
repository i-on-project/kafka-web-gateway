package com.isel.record.router.kafkaStreamsExperimentations

import com.isel.record.router.utilsExperimentations.KafkaStreamsUtils
import com.isel.record.router.utilsExperimentations.TopicKeys
import com.isel.record.router.utilsExperimentations.TopicKeysArraySerDe
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.GlobalKTable
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.apache.kafka.streams.state.ValueAndTimestamp
import org.springframework.stereotype.Component

@Component
class UseCase(
    val utils: KafkaStreamsUtils,
    val kStreamsHandler: KStreamsHandler,
    val globalKTables: GlobalKTables
) {
    private val keyStorage: ReadOnlyKeyValueStore<String, ValueAndTimestamp<Array<TopicKeys>>>? = null //= startStore()
    companion object {
        private val STORE_TOPIC: String = "store-topic"
        private val STORE_NAME: String = "store-name"
    }

    fun populateStore() {
        val producer: KafkaProducer<String, Array<TopicKeys>> = KafkaProducer(utils.getProducerDefaultProperties())

        producer.send(ProducerRecord(
            STORE_TOPIC, "gateway-a", arrayOf(
            TopicKeys("use-case-a", arrayOf(
                1L, 0L
            )),
            TopicKeys("use-case-b", arrayOf(
                10L, 0L
            ))
        )))
        producer.send(ProducerRecord(
            STORE_TOPIC, "gateway-b", arrayOf(
            TopicKeys("use-case-a", arrayOf(
                2L, 0L
            ))
        )))
        producer.send(ProducerRecord(
            STORE_TOPIC, "gateway-c", arrayOf(
            TopicKeys("use-case-a", arrayOf(
                3L, 300L
            )),
            TopicKeys("use-case-b", arrayOf(
                30L, 300L
            ))
        )))
    }

    /**
     * Creates and/or accesses the GlobalKTable responsible for storing the keys and topics to know to which gateway topic it goes.
     *
     */
    final fun startStore(): ReadOnlyKeyValueStore<String, ValueAndTimestamp<Array<TopicKeys>>> {
        loggerConsumer()
        val builder = StreamsBuilder()

        val gatewaysKeys: GlobalKTable<String, Array<TopicKeys>> = builder.globalTable(
            STORE_TOPIC,
            Materialized.`as`<String, Array<TopicKeys>, KeyValueStore<Bytes, ByteArray>>(STORE_NAME)
                .withKeySerde(Serdes.String())
                .withValueSerde(TopicKeysArraySerDe())
        )

        val stream = KafkaStreams(builder.build(), utils.getStreamDefaultProperties())
        utils.streamsMap.put(STORE_TOPIC,stream)

        stream.cleanUp()
        stream.start()

        // Creates a local state store (a file/db) in the current machine. Also good for querying.
        val store: ReadOnlyKeyValueStore<String, ValueAndTimestamp<Array<TopicKeys>>> = stream.store(
            StoreQueryParameters.fromNameAndType(STORE_NAME, QueryableStoreTypes.timestampedKeyValueStore())
        )

        store.all().forEach { record ->
            println("record key[${record.key}]")
            println("record values:")
            for (topicKeys in record.value.value()) {
                println("TOPIC=[${topicKeys.topic}]")
                for (key in topicKeys.keys) {
                    println("key=[$key]")
                }
            }
            println("----------------------------")
        }


        Runtime.getRuntime().addShutdownHook(Thread(stream::close))

        return store
    }

    fun startStreamUsingStore(inputTopic: String) {
        val builder = StreamsBuilder()
        val inputStream: KStream<Long, String> = builder.stream(inputTopic, Consumed.with(Serdes.Long(), Serdes.String()))

        /* for (gatewayEntry in keyStorage.all().iterator()) {
            KafkaStreamsUtils.printlnBetweenColoredLines("gateway entry key[${gatewayEntry.key}]", KafkaStreamsUtils.PURPLE_TEXT)

            inputStream.filter {key, value -> isKeyForGateway(key, gatewayEntry.key, inputTopic)}.to(gatewayEntry.key)
        }*/

        val stream = KafkaStreams(builder.build(), utils.getStreamDefaultProperties())
        utils.streamsMap.put(inputTopic ,stream)

        stream.start()

        KafkaStreamsUtils.getTopologyMetrics(stream)

        Runtime.getRuntime().addShutdownHook(Thread(stream::close))
    }

    private fun isKeyForGateway(key: Long, gateway: String, inputTopic: String): Boolean {
        KafkaStreamsUtils.printlnBetweenColoredLines("isKeyForGateway key[$key] gateway[$gateway] inputTopic[$inputTopic]", KafkaStreamsUtils.PURPLE_TEXT)
        /*for (topicKeys in keyStorage.get(gateway).value()) {
            if (topicKeys.topic == inputTopic) {
                for (topicKey in topicKeys.keys) {
                    if (topicKey == key) {
                        return true
                    }
                }
            }
        }*/
        return false
    }

    fun checkStreamMetadata(topic: String) {
        val stream: KafkaStreams? = utils.streamsMap.get(topic)
        if (stream == null) {
            println("checkStreamMetadata for stream of topic [$topic] failed.")
            return
        }
        checkStreamMetadata(stream)
    }

    fun checkStreamMetadata(stream: KafkaStreams) {

        stream.metrics().forEach {
            println("metric name [${it.key}]")
            println("metric value name [${it.value.metricName().name()}], group [${it.value.metricName().group()}], description [${it.value.metricName().description()}] and tags [${it.value.metricName().tags().toString()}]")
            println("metric value:")
            println(it.value.metricValue().toString())
        }

    }

    /**
     * Function responsible for logging to the console the received records
     * in the output topics (for testing purposes).
     */
    fun loggerConsumer() {
        utils.loggerConsumer(listOf("gateway-a", "gateway-b", "gateway-c"))
    }


}