package com.isel.kafkastreamsmoduledemo.kafkaStreams

import com.fasterxml.jackson.databind.ObjectMapper
import com.isel.kafkastreamsmoduledemo.utiils.TopicKeys
import com.isel.kafkastreamsmoduledemo.utiils.TopicKeysArraySerDe
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.*
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.processor.RecordContext
import org.apache.kafka.streams.processor.To
import org.apache.kafka.streams.processor.api.Processor
import org.apache.kafka.streams.processor.api.ProcessorContext
import org.apache.kafka.streams.processor.api.Record
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.Stores
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap


@Component
class KStreamsHandler(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String // TODO: maybe change to db or kafka topic
) {

    private lateinit var plannerStream: KafkaStreams
    private val streamsMap: ConcurrentHashMap<String, KafkaStreams> = ConcurrentHashMap()
    private var testGatewayKeys: ConcurrentHashMap<String, Array<TopicKeys>> = ConcurrentHashMap()

    companion object {
        private val KEY_FILTER_STORE = "key-filter-store"
        private val KEY_FILTER_TOPIC = "key-filter-topic"
        private val DEFAULT_STREAM_ID = "gateway-topics-filter-stream"


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
        inputStream.to {key, value, recordContext -> gatewayKeyFilter(key, value, recordContext)[0]}

        val stream = KafkaStreams(builder.build(), getStreamDefaultProperties())
        streamsMap.put(topic ,stream)
        println("startGatewayFiltersStream function ending")
    }




    private fun gatewayKeyFilter(key: Long, value: String, recordContext: RecordContext): Array<String> {
        val outputTopics: Array<String> = emptyArray()
        for (entry in testGatewayKeys) {
            for (topicKeysArray in entry.value) {
                if (topicKeysArray.keys.contains(key)) {
                    outputTopics.plus(topicKeysArray.topic)
                }
            }
        }
        return outputTopics
    }

    private fun getStreamDefaultProperties(): Properties {
        val props = Properties()
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, DEFAULT_STREAM_ID)
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1) // This value is only for testing, should be around maybe 20 or more in production. Default is 100
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Long().javaClass)
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().javaClass)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
        return props
    }

    fun startStreaming(id: String): String {
        println("startStreaming id: [$id]")

        if (streamsMap.contains(id)) {
            streamsMap.get(id)?.close()
            streamsMap.remove(id)
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
        streamsMap.put(id, streams)

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

    fun loggerConsumer() {

        if (streamsMap.contains("logger-stream")) {
            streamsMap.get("logger-stream")?.close()
            streamsMap.remove("logger-stream")
            return
        }

        val props = Properties()
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "fu")
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer::class.java.name)
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        val consumer: KafkaConsumer<Long, String> = KafkaConsumer(props)

        consumer.subscribe(listOf("even", "uneven"))



        while (true) {
            consumer.poll(Duration.ofSeconds(5)).forEach { record ->
                println("[${System.currentTimeMillis()}] - Consumer key: [${record.key()}] and value[${record.value()}] from topic:[${record.topic()}] and timestamp [${record.timestamp()}]")
            }
        }
    }

    fun closeConsumerLogger() {
        streamsMap.remove("logger-stream")
    }

    fun loggerStream() {
        if (streamsMap.contains("logger-stream")) {
            streamsMap.get("logger-stream")?.close()
            streamsMap.remove("logger-stream")
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
        streamsMap.put("logger-stream", streams)

        streams.cleanUp()
        streams.start()
    }

    fun closeStream(id: String): String {
        println("closeStream id: [$id]")
        return if(streamsMap.remove(id) != null) "removed" else "no key"
    }


}