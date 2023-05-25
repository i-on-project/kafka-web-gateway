package com.isel.ps.gateway.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.isel.ps.gateway.Utils
import com.isel.ps.gateway.model.GatewayTypes.*
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.Headers

class GatewayTypes {

    /**
     * Schema for consumed Kafka messages. Key and values must be serialized by the client since
     * it's the only one who knows it.
     *
     * @param gatewayMessageID
     *   The message ID for this record.
     * @param topic
     *   The topic the message came from.
     * @param partition
     *   The topic partition the message came from.
     * @param offset
     *   The topic partition offset for the message.
     * @param timestamp
     *   The timestamp for when the message was written to Kafka.
     * @param key
     *   The message key.
     * @param value
     *   The message value.
     * @param headers
     *   The headers applied to the message in Kafka.
     */
    data class GatewayConsumerRecord(
        val gatewayMessageID: String, val topic: String, val partition: Int, val offset: Long,
        val timestamp: Long, val key: String, val value: String, val headers: Headers
    ) {
        companion object {
            fun build(record: ConsumerRecord<String, String>): GatewayConsumerRecord {
                return GatewayConsumerRecord(
                    String(record.headers().lastHeader(Utils.GATEWAY_MESSAGE_ID).value()),
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.timestamp(),
                    record.key(),
                    record.value(),
                    record.headers()
                )
            }
        }
    }

    /**
     * Schema for messages to produce into Kafka topics via the WebSocket
     * Gateway.
     *
     * @param key
     *   The message key.
     * @param value
     *   The message value.
     * @param headers
     *   The headers of the message in Kafka(usually considered metadata).
     * @param clientMessageId
     *   Message identifier given by the client to uniquely identify the message.
     */
    data class GatewayProducerRecord(
        val clientMessageId: String, val key: String, val value: String, val headers: Headers
    )

    /**
     * Schema for confirmation messages sent back through the producer socket when
     * a message has been successfully sent to Kafka.
     *
     * @param topic
     *   The topic the message was written to.
     * @param partition
     *   The topic partition the message was written to.
     * @param offset
     *   The topic partition offset for the given message.
     * @param timestamp
     *   The timestamp for when the message was written to Kafka.
     * @param clientMessageId
     *   Message identifier given by the client to uniquely identify the message.
     */
    data class ProducerResult(
        val clientMessageId: String, val topic: String, val partition: Int, val offset: Long, val timestamp: Long
    )

    /**
     * Schema to use when committing consumed Kafka messages through an
     * WebSocket.
     *
     * @param gatewayMessageID
     *   The message ID to commit.
     */
    data class CommitMessage(val gatewayMessageID: String)

    /* ------------------------------------------------------------------- */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
    @JsonSubTypes(
        JsonSubTypes.Type(value = Subscribe::class, name = "subscribe"),
        JsonSubTypes.Type(value = Consume::class, name = "consume"),
        JsonSubTypes.Type(value = Publish::class, name = "publish"),
        JsonSubTypes.Type(value = Pause::class, name = "pause"),
        JsonSubTypes.Type(value = Resume::class, name = "resume"),
        JsonSubTypes.Type(value = Ack::class, name = "ack"),
        JsonSubTypes.Type(value = Err::class, name = "error")
    )
    open class Command(@JsonProperty("type") val type: String)

    data class ClientMessage(
        val messageID: String,
        @JsonDeserialize(using = CommandDeserializer::class)
        val command: Command
    ) {
        constructor() : this("", Command(""))
    }

    data class Subscribe(val topics: List<TopicType>, val startConsuming: Boolean = false) : Command("subscribe") {
        constructor() : this(emptyList(), false)
    }

    data class Consume(val maxQuantity: Int?, val scale: String?) : Command("consume") {
        constructor() : this(null, null)
    }

    data class Publish(val topic: String, val key: String, val value: String) : Command("publish")

    data class Pause(val topics: List<TopicType>) : Command("pause")

    data class Resume(val topics: List<TopicType>) : Command("resume")

    class Ack : Command("ack")

    data class Err(val message: String?) : Command("error")

    data class TopicType(val topic: String, val partition: Int?)
}

class CommandDeserializer : JsonDeserializer<Command>() {
    override fun deserialize(
        p: JsonParser?,
        ctxt: DeserializationContext?
    ): Command {
        val node = p?.codec?.readTree<JsonNode>(p)

        return when (val type = node?.get("type")?.asText()) {
            "subscribe" -> p.codec?.treeToValue(node, Subscribe::class.java) as Command
            "consume" -> p.codec?.treeToValue(node, Consume::class.java) as Command
            "publish" -> p.codec?.treeToValue(node, Publish::class.java) as Command
            "pause" -> p.codec?.treeToValue(node, Pause::class.java) as Command
            "resume" -> p.codec?.treeToValue(node, Resume::class.java) as Command
            "ack" -> p.codec?.treeToValue(node, Ack::class.java) as Command
            "error" -> p.codec?.treeToValue(node, Err::class.java) as Command
            else -> throw IllegalArgumentException("Unknown command type: $type")
        }
    }
}

class TopicTypeDeserializer : JsonDeserializer<TopicType>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): TopicType {
        val node = p?.codec?.readTree<JsonNode>(p)
        val topic = node?.get("topic")?.asText()
        val partition = node?.get("partition")?.asInt()

        return TopicType(topic!!, partition)
    }
}