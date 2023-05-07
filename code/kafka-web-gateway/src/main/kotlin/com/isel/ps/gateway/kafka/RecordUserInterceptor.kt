package com.isel.ps.gateway.kafka

import com.isel.ps.gateway.model.GatewayTypes.GatewayConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerInterceptor
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition

class RecordUserInterceptor : ConsumerInterceptor<String, String> {

    companion object {
        const val USER_ID = "INTERCEPTOR_USER_ID_KEY"
    }

    private var userId: String? = null

    override fun onConsume(records: ConsumerRecords<String, String>): ConsumerRecords<String, String> {
        val filteredRecords = mutableMapOf<TopicPartition, MutableList<ConsumerRecord<String, String>>>()
        for (record in records) {

            val recordParsed = GatewayConsumerRecord.build(record)

            if (userId == recordParsed.key) {
                filteredRecords.merge(
                    TopicPartition(record.topic(), record.partition()),
                    mutableListOf(record)
                ) { _, value ->
                    value.add(record)
                    value
                }
            }
        }
        return ConsumerRecords(filteredRecords)
    }

    override fun onCommit(offsets: MutableMap<TopicPartition, OffsetAndMetadata>?) {
        // Do nothing
    }

    override fun close() {
        // Do nothing
    }

    override fun configure(configs: MutableMap<String, *>?) {
        userId = configs?.get(USER_ID).toString()
    }
}
