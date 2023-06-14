package com.isel.kafkastreamsmoduledemo.recordRouter

import com.isel.kafkastreamsmoduledemo.utilsExperimentations.KafkaStreamsUtils
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class RecordRouterUtils(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String
) {

    fun systemTopicStreamProperties(offsetConfig: String = "latest", streamId: String): Properties {
        val props = Properties()
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, streamId)
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1) // This value is only for testing, should be around maybe 20 or more in production. Default is 100
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().javaClass)
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().javaClass)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetConfig)
        return props
    }

    fun gatewayKeysStreamProperties(offsetConfig: String = "earliest", streamId: String): Properties {
        val props = Properties()
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, streamId)
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1) // This value is only for testing, should be around maybe 20 or more in production. Default is 100
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().javaClass)
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().javaClass)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetConfig)
        return props
    }

    fun topicRecordRouterStreamProperties(offsetConfig: String, streamId: String): Properties {
        val props = Properties()
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, streamId)
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1) // This value is only for testing, should be around maybe 20 or more in production. Default is 100
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().javaClass)
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().javaClass)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetConfig)
        return props

    }

}