package com.isel.kafkastreamsmoduledemo.recordRouter

import org.apache.kafka.streams.KafkaStreams
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

data class StreamPlan (
    var stream: KafkaStreams,
    val initializer: () -> KafkaStreams
)
@Component
class StreamsStorage {
    private val defaultRoutingStreams: ConcurrentHashMap<String, StreamPlan> = ConcurrentHashMap()
    private var gatewaysKeysStreams: ConcurrentHashMap<String, StreamPlan> = ConcurrentHashMap()
    private var systemTopicStream: StreamPlan? = null

    fun addDefaultRoutingStream(id: String, stream: KafkaStreams, initializer: () -> KafkaStreams): StreamPlan? {
        return defaultRoutingStreams.putIfAbsent(id, StreamPlan(stream, initializer))
    }

    fun addGatewayKeysStream(id: String, stream: KafkaStreams, initializer: () -> KafkaStreams): StreamPlan? {
        return gatewaysKeysStreams.putIfAbsent(id, StreamPlan(stream, initializer))
    }

    fun removeDefaultRoutingStream(id: String): StreamPlan? {
        return defaultRoutingStreams.remove(id)
    }

    fun restartDefaultRoutingStream(id: String) {
        defaultRoutingStreams.compute(id) { _, streamPlan ->
            if(!streamPlan?.stream?.close(Duration.ofMillis(300))!!) {
                println("restartStream: couldn't close stream id [${id}] in time.")
            }
            streamPlan.stream = streamPlan.initializer()
            streamPlan
        }
    }

    fun restartAllDefaultRoutingStreams() {
        defaultRoutingStreams.forEach { (key, _) ->
            restartDefaultRoutingStream(key)
        }
    }

    fun setSystemTopicStream(stream: KafkaStreams, initializer: () -> KafkaStreams) {
        systemTopicStream = StreamPlan(stream, initializer)
    }
}