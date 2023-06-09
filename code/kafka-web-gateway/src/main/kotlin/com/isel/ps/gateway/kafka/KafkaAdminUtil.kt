package com.isel.ps.gateway.kafka

import org.apache.kafka.clients.admin.*
import org.apache.kafka.common.errors.TopicExistsException
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ExecutionException

@Configuration
class KafkaAdminUtil(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String
) {
    private val adminClient: Admin

    init {
        val adminConfig: MutableMap<String, Any> = HashMap()
        adminConfig[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        adminClient = Admin.create(adminConfig)
    }

    fun createTopic(topicName: String, numPartitions: Int, replicationFactor: Short) {
        val newTopic = NewTopic(topicName, numPartitions, replicationFactor)
        val options = CreateTopicsOptions().timeoutMs(5000)
        try {
            adminClient.createTopics(setOf(newTopic), options).all().get()
        } catch (e: ExecutionException) {
            if (e.cause is TopicExistsException) {
                println("Topic $topicName already exists")
            } else {
                throw e
            }
        }
    }

    fun deleteTopic(topicName: String) {
        try {
            val options = DeleteTopicsOptions().timeoutMs(5000)
            adminClient.deleteTopics(setOf(topicName), options).all().get()
        } catch (e: ExecutionException) {
            if (e.cause is UnknownTopicOrPartitionException) {
                println("Topic $topicName does not exist")
            } else {
                throw e
            }
        }
    }

    fun deleteTopics(topics: Collection<String?>?): DeleteTopicsResult {
        val options = DeleteTopicsOptions().timeoutMs(5000)
        return adminClient.deleteTopics(topics, options)
    }

    fun describeTopic(topicName: String): TopicDescription? {
        val options = DescribeTopicsOptions().timeoutMs(5000)
        val result = adminClient.describeTopics(setOf(topicName), options)
        val topicDescriptions = result.allTopicNames().get()
        return if (topicDescriptions.containsKey(topicName)) {
            topicDescriptions[topicName]
        } else {
            throw ExecutionException(
                UnknownTopicOrPartitionException("Topic $topicName does not exist")
            )
        }
    }

    fun listTopics(): Set<String> {
        val options = ListTopicsOptions().timeoutMs(5000)
        val result = adminClient.listTopics(options)
        return result.names().get()
    }

    fun hasTopic(topicName: String): Boolean {
        val options = DescribeTopicsOptions().timeoutMs(5000)
        val result = adminClient.describeTopics(setOf(topicName), options)
        val topicDescriptions = result.allTopicNames().get()
        return topicDescriptions.containsKey(topicName)
    }

    fun closeAdminClient() {
        adminClient.close()
    }
}
