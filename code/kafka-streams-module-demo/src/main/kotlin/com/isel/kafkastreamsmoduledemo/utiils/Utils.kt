package com.isel.kafkastreamsmoduledemo.utiils

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.*
import org.apache.kafka.common.serialization.Serdes.WrapperSerde

data class TopicKeys(
    val topic: String,
    val keys: Array<Long>
)

class TopicKeysArrayDeserializer: Deserializer<Array<TopicKeys>> {
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

class TopicKeysArraySerializer : Serializer<Array<TopicKeys>> {
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

class TopicKeysArraySerDe: Serde<Array<TopicKeys>> {
    override fun serializer(): Serializer<Array<TopicKeys>> = TopicKeysArraySerializer()

    override fun deserializer(): Deserializer<Array<TopicKeys>> = TopicKeysArrayDeserializer()

}
