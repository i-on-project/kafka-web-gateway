package com.isel.kafkastreamsmoduledemo.kafkaStreams

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.processor.RecordContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class KStreamsHandler(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String // TODO: maybe change to db or kafka topic
) {

    companion object {
        private val streamsMap: ConcurrentHashMap<String, KafkaStreams> = ConcurrentHashMap()
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

        inputStream.to { key, value, recordContext -> evenFilter(key, value, recordContext) }


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