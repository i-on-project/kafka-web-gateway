package com.isel.kafkastreamsmoduledemo.kafkaStreamsExperimentations

import com.isel.kafkastreamsmoduledemo.utils.KafkaStreamsUtils
import com.isel.kafkastreamsmoduledemo.utils.KafkaStreamsUtils.Companion.KEY_FILTER_STORE
import com.isel.kafkastreamsmoduledemo.utils.KafkaStreamsUtils.Companion.KEY_FILTER_TOPIC
import com.isel.kafkastreamsmoduledemo.utils.TopicKeys
import com.isel.kafkastreamsmoduledemo.utils.TopicKeysArraySerDe
import org.apache.kafka.clients.consumer.KafkaConsumer
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
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration
import kotlin.concurrent.thread

@Component
class GlobalKTables (
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String, // TODO: maybe change to db or kafka topic
    private val utils: KafkaStreamsUtils
) {
    private lateinit var tableStore: ReadOnlyKeyValueStore<String, ValueAndTimestamp<Array<TopicKeys>>>

    fun globalTable() {

        val builder = StreamsBuilder()


        // Create a global table for gateways topics keys. The data from this global table
        // will be fully replicated on each instance of this application.
        val gatewaysKeys: GlobalKTable<String, Array<TopicKeys>> = builder.globalTable(
            KEY_FILTER_STORE,
            Materialized.`as`<String, Array<TopicKeys>, KeyValueStore<Bytes, ByteArray>>(KEY_FILTER_STORE)
                .withKeySerde(Serdes.String())
                .withValueSerde(TopicKeysArraySerDe())
        )

        val stream = KafkaStreams(builder.build(), utils.getStreamDefaultProperties())
        utils.streamsMap.put(KEY_FILTER_TOPIC ,stream)

        stream.start()

        Runtime.getRuntime().addShutdownHook(Thread(stream::close))
    }

    fun accessGlobalTable() {
        val builder = StreamsBuilder()

        val inputStream: KStream<String, Array<TopicKeys>> = builder.stream(KEY_FILTER_TOPIC, Consumed.with(Serdes.String(), TopicKeysArraySerDe()))

        inputStream.peek { key, value ->
            println("record key[${key}]")
            println("record values:")
            for (topicKeys in value) {
                println("TOPIC=[${topicKeys.topic}]")
                for (topicKey in topicKeys.keys) {
                    println("key=[$topicKey]")
                }
            }
            println("----------------------------")
        }


        val stream = KafkaStreams(builder.build(), utils.getStreamDefaultProperties())
        stream.start()
        tableStore = stream.store(
            StoreQueryParameters.fromNameAndType(KEY_FILTER_STORE, QueryableStoreTypes.timestampedKeyValueStore())
        )
    }

    fun populateGlobalTable() {
        val producer: KafkaProducer<String, Array<TopicKeys>> = KafkaProducer(utils.getProducerDefaultProperties())
        val value = arrayOf(
            TopicKeys("topicA", arrayOf(
                3L, 0L
            ))
        )
        producer.send(ProducerRecord(KEY_FILTER_TOPIC, "gateway01", value))
    }

    fun consumeSaidTablesTopic() {
        val consumer: KafkaConsumer<String, Array<TopicKeys>> = KafkaConsumer(utils.getConsumerDefaultProperties())
        consumer.subscribe(arrayListOf(KEY_FILTER_TOPIC))

        thread {
            while (true) {
                consumer.poll(Duration.ofSeconds(5)).forEach { record ->
                    println("record key[${record.key()}]")
                    println("record values:")
                    for (topicKeys in record.value()) {
                        println("TOPIC=[${topicKeys.topic}]")
                        for (key in topicKeys.keys) {
                            println("key=[$key]")
                        }
                    }
                    println("----------------------------")
                }

            }
        }
    }

    fun printGlobalTable() {
        tableStore.all().forEach { record ->
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

    }
}