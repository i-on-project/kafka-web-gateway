package com.isel.kafkastreamsmoduledemo.recordRouter

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.streams.KafkaStreams
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

data class StreamPlan (
    var stream: KafkaStreams,
    val initializer: () -> KafkaStreams
)
data class ConsumerPlan (
    var consumer: KafkaConsumer<String, String>,
    val initializer: () -> KafkaConsumer<String, String>
)
@Component
class StreamsStorage {
    private val defaultRoutingStreams: ConcurrentHashMap<String, StreamPlan> = ConcurrentHashMap()
    private var gatewaysKeysConsumer: ConsumerPlan? = null
    private var systemTopicConsumer: ConsumerPlan? = null

    fun addDefaultRoutingStream(id: String, stream: KafkaStreams, initializer: () -> KafkaStreams): StreamPlan? {
        return defaultRoutingStreams.putIfAbsent(id, StreamPlan(stream, initializer))
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

    fun setSystemTopicConsumer(consumer: KafkaConsumer<String, String>, initializer: () -> KafkaConsumer<String, String>) {
        systemTopicConsumer = ConsumerPlan(consumer, initializer)
    }

    fun setGatewayKeysConsumer(consumer: KafkaConsumer<String, String>, initializer: () -> KafkaConsumer<String, String>) {
        gatewaysKeysConsumer = ConsumerPlan(consumer, initializer)
    }
    fun getGatewayKeysConsumer(): KafkaConsumer<String, String>? {
        return gatewaysKeysConsumer?.consumer
    }
}