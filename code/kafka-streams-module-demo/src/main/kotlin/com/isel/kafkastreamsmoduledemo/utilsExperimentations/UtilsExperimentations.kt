package com.isel.kafkastreamsmoduledemo.utilsExperimentations

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.*
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

@Component
class KafkaStreamsUtils(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String // TODO: maybe change to db or kafka topic
) {
    val streamsMap: ConcurrentHashMap<String, KafkaStreams> = ConcurrentHashMap()
    val consumersMap: ConcurrentHashMap<String, KafkaConsumer<Any, Any>> = ConcurrentHashMap()
    companion object {
        val KEY_FILTER_STORE = "key-filter-store"
        val KEY_FILTER_TOPIC = "key-filter-topic"
        const val DEFAULT_STREAM_ID = "gateway-id-0"
        const val GREEN_TEXT = "\u001B[32m"
        const val PURPLE_TEXT = "\u001B[35m"
        const val YELLOW_TEXT = "\u001B[33m"
        const val RESET_TEXT_COLOR = "\u001B[0m"

        fun getTopologyMetrics(stream: KafkaStreams) {
            stream.metrics().forEach {
                if (it.key.name() == "topology-description") {
                    printlnWithColor("${it.value.metricValue()}", PURPLE_TEXT)
                }
            }
        }

        fun printlnBetweenColoredLines(logContent: String, textColor: String = PURPLE_TEXT) {
            println("${YELLOW_TEXT}******************************${RESET_TEXT_COLOR}")
            println("${textColor}${logContent}${RESET_TEXT_COLOR}")
            println("${YELLOW_TEXT}******************************${RESET_TEXT_COLOR}")
        }
        fun printlnWithColor(logContent: String, textColor: String = GREEN_TEXT) {
            println("${textColor}${logContent}${RESET_TEXT_COLOR}")
        }
    }
    fun getStreamDefaultProperties(offsetConfig: String = "latest", streamId: String = DEFAULT_STREAM_ID): Properties {
        val props = Properties()
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, streamId)
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1) // This value is only for testing, should be around maybe 20 or more in production. Default is 100
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Long().javaClass)
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().javaClass)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetConfig)
        return props
    }

    fun getProducerDefaultProperties(): Properties {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = TopicKeysArraySerializer::class.java.name
        return props
    }

    fun getConsumerDefaultProperties(groupId: String = "123123"): Properties {
        val props = Properties()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = TopicKeysArrayDeserializer::class.java.name
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        return props
    }

    fun loggerConsumer(topics: List<String>) {
        if (topics.isEmpty()) {
            println("Failed to create a loggerConsumer as there were no topics provided..")
            return
        }

        if (consumersMap.contains("logger-consumer")) {
            consumersMap.get("logger-consumer")?.close()
            consumersMap.remove("logger-consumer")
            return
        }

        val props = Properties()
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "logger-consumer")
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer::class.java.name)
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        val consumer: KafkaConsumer<Long, String> = KafkaConsumer(props)

        consumer.subscribe(topics)

        thread {
            while (true) {
                consumer.poll(Duration.ofSeconds(5)).forEach { record ->
                    printlnWithColor("[${System.currentTimeMillis()}] - Consumer key: [${record.key()}] and value[${record.value()}] from topic:[${record.topic()}] and timestamp [${record.timestamp()}]")
                }
            }
        }

    }
}
data class TopicKeys(
    val topic: String,
    val keys: Array<Long>
) {
}



class TopicKeysArrayDeserializer: Deserializer<Array<TopicKeys>> {
    override fun deserialize(topic: String?, data: ByteArray?): Array<TopicKeys>? {
        return try {
            if (data == null) {
                println("Null received at deserializing")
                return null
            }
            println("Deserializing...")
            var result: Array<TopicKeys> = emptyArray()
            val dataInputStream = DataInputStream(ByteArrayInputStream(data))

            while (dataInputStream.available() > 0) {
                val topicStringLength: Int = dataInputStream.readInt()
                val stringBuilder: StringBuilder = StringBuilder()
                for (i in 1..topicStringLength) {
                    stringBuilder.append(dataInputStream.readChar())
                }
                val topic: String = stringBuilder.toString()
                val keyNr: Int = dataInputStream.readInt()
                var keys: Array<Long> = emptyArray()
                for (i in 1..keyNr) {
                    keys = keys.plus(dataInputStream.readLong())
                }
                result = result.plus(TopicKeys(topic, keys))
            }
            result
        } catch (e: Exception) {
            throw SerializationException("Error when deserializing byte[] to TopicKeys[]")
        }
    }

}

class TopicKeysArraySerializer : Serializer<Array<TopicKeys>> {
    override fun serialize(topic: String?, data: Array<TopicKeys>?): ByteArray? {
        return try {
            val out = ByteArrayOutputStream()
            val dataOutputStream =DataOutputStream(out)
            if (data == null) {
                println("Null received at serializing")
                return null
            }
            println("Serializing...")
            for (topicKeys in data) {
                dataOutputStream.writeInt(topicKeys.topic.length)
                for (i in 0 until topicKeys.topic.length) {
                    dataOutputStream.writeChar(topicKeys.topic.get(i).code)
                }
                dataOutputStream.writeInt(topicKeys.keys.size)
                for (key in topicKeys.keys) {
                    dataOutputStream.writeLong(key)
                }
            }
            dataOutputStream.flush()
            out.toByteArray()
        } catch (e: java.lang.Exception) {
            throw SerializationException("Error when serializing MessageDto to byte[]")
        }
    }
}

class TopicKeysArraySerDe: Serde<Array<TopicKeys>> {
    override fun serializer(): Serializer<Array<TopicKeys>> = TopicKeysArraySerializer()

    override fun deserializer(): Deserializer<Array<TopicKeys>> = TopicKeysArrayDeserializer()

}
