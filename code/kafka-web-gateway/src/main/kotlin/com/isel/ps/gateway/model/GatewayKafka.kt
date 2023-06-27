import com.isel.ps.gateway.Utils
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.Headers

class GatewayKafka {
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

    data class SystemGateway(
        val gatewayId: String,
        val keysTopicName: String,
        val clientsTopicName: String,
        val commandsTopicName: String?
    )

    data class GatewayTopicKeys(
        val keys: List<String>?,
        val allKeys: Boolean?
    )
}
