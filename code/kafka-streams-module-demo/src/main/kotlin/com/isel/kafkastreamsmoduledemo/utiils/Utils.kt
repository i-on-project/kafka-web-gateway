package com.isel.kafkastreamsmoduledemo.utiils

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
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
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class KafkaStreamsUtils(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String // TODO: maybe change to db or kafka topic
) {
    val streamsMap: ConcurrentHashMap<String, KafkaStreams> = ConcurrentHashMap()
    companion object {
        val KEY_FILTER_STORE = "key-filter-store"
        val KEY_FILTER_TOPIC = "key-filter-topic"
        const val DEFAULT_STREAM_ID = "gateway-topics-filter-stream"
    }
    fun getStreamDefaultProperties(offsetConfig: String = "latest"): Properties {
        val props = Properties()
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, DEFAULT_STREAM_ID)
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
}
data class TopicKeys(
    val topic: String,
    val keys: Array<Long>
) {
}

class TopicKeysArrayDeserializer2: Deserializer<Array<TopicKeys>> {
    private val objectMapper = ObjectMapper()
    override fun deserialize(topic: String?, data: ByteArray?): Array<TopicKeys>? {
        return try {
            if (data == null) {
                println("Null received at deserializing")
                return null
            }
            println("Deserializing...")
            objectMapper.readValue(String(data, charset("UTF-8")), Array<TopicKeys>::class.java)
        } catch (e: Exception) {
            throw SerializationException("Error when deserializing byte[] to TopicKeys[]")
        }
    }

}

class TopicKeysArraySerializer2 : Serializer<Array<TopicKeys>> {
    private val objectMapper = ObjectMapper()
    override fun serialize(topic: String?, data: Array<TopicKeys>?): ByteArray? {
        return try {
            if (data == null) {
                println("Null received at serializing")
                return null
            }
            println("Serializing...")
            objectMapper.writeValueAsBytes(data)
        } catch (e: java.lang.Exception) {
            throw SerializationException("Error when serializing MessageDto to byte[]")
        }
    }
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
                println("topic")
                val stringBuilder: StringBuilder = StringBuilder()
                for (i in 1..topicStringLength) {
                    println("char")
                    stringBuilder.append(dataInputStream.readChar())
                }
                val topic: String = stringBuilder.toString()
                println("keynr")
                val keyNr: Int = dataInputStream.readInt()
                println("keys")
                var keys: Array<Long> = emptyArray()
                for (i in 1..keyNr) {
                    println("key")
                    keys = keys.plus(dataInputStream.readLong())
                }
                println("adding")
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
