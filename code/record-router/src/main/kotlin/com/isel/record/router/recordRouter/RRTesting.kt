package com.isel.record.router.recordRouter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.clients.admin.*
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.errors.TopicExistsException
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Component
class RRTesting(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String,
    private val utils: RecordRouterUtils
) {
    private val adminClient: Admin

    init {
        val adminConfig: MutableMap<String, Any> = HashMap()
        adminConfig[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        adminClient = Admin.create(adminConfig)
    }

    private val mapper = jacksonObjectMapper()
    private final val consumerExecutor: ExecutorService = Executors.newFixedThreadPool(20)

    fun test() {


        val systemTopic: String = "SYSTEM_TOPIC"
        val inputTopicA: String = "input-topic-a"
        val inputTopicB: String = "input-topic-b"

        runConsumer(systemTopic)
        runConsumer(inputTopicA)
        runConsumer(inputTopicB)
        runConsumer("gateway-01-clients-topic")
        runConsumer("gateway-01-keys-topic")
        runConsumer("gateway-02-clients-topic")
        runConsumer("gateway-02-keys-topic")
        val props = Properties()
        props["bootstrap.servers"] = bootstrapServers
        props["key.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
        props["value.serializer"] = "org.apache.kafka.common.serialization.StringSerializer"
        val producer: KafkaProducer<String, String> = KafkaProducer<String, String>(props)

        val newGatewayKeyTopic = SystemHub("id1", "gateway-01-keys-topic", "gateway-01-clients-topic", "")
        val newGatewayKeyTopicJson: String = mapper.writeValueAsString(newGatewayKeyTopic)
        utils.printRed("TEST newGatewayKeyTopicJson as string: \n $newGatewayKeyTopicJson")

        producer.send(ProducerRecord(systemTopic, "new-gateway", newGatewayKeyTopicJson))

        Thread.sleep(1000)

        producer.send(ProducerRecord(newGatewayKeyTopic.keysTopicName, inputTopicA, mapper.writeValueAsString(
            HubTopicKeys(listOf("0", "1"), null)
        )))

        Thread.sleep(2000)

        producer.send(ProducerRecord(inputTopicA, "0", "testvalue 0"))
        producer.send(ProducerRecord(inputTopicA, "0", "testvalue 1"))
        producer.send(ProducerRecord(inputTopicA, "0", "testvalue 2"))
        producer.send(ProducerRecord(inputTopicA, "0", "testvalue 3"))
        producer.send(ProducerRecord(inputTopicA, "0", "testvalue 4"))

        Thread.sleep(6000)

        val newGatewayKeyTopic2 = SystemHub("id2", "gateway-02-keys-topic", "gateway-02-clients-topic", "")
        val newGatewayKeyTopicJson2: String = mapper.writeValueAsString(newGatewayKeyTopic2)
        producer.send(ProducerRecord(systemTopic, "new-gateway", newGatewayKeyTopicJson2))

        Thread.sleep(1000)

        producer.send(ProducerRecord(newGatewayKeyTopic2.keysTopicName, inputTopicA, mapper.writeValueAsString(
            HubTopicKeys(listOf("1", "2"), null)
        )))
        //producer.send(ProducerRecord(newGatewayKeyTopic2.keysTopicName, inputTopicB, mapper.writeValueAsString(GatewayTopicKeys(listOf("5", "6"), null))))
        producer.send(ProducerRecord(newGatewayKeyTopic2.keysTopicName, inputTopicB, mapper.writeValueAsString(
            HubTopicKeys(listOf(), true)
        )))

        Thread.sleep(2000)

        producer.send(ProducerRecord(inputTopicA, "0", "testvalue 10"))
        producer.send(ProducerRecord(inputTopicA, "0", "testvalue 11"))
        producer.send(ProducerRecord(inputTopicA, "1", "testvalue 12"))
        producer.send(ProducerRecord(inputTopicA, "1", "testvalue 13"))
        producer.send(ProducerRecord(inputTopicA, "2", "testvalue 14"))

        producer.send(ProducerRecord(inputTopicB, "1", "no 13"))
        producer.send(ProducerRecord(inputTopicB, "2", "no 14"))
        producer.send(ProducerRecord(inputTopicB, "5", "yes 13"))
        producer.send(ProducerRecord(inputTopicB, "6", "yes 14"))

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

    fun runConsumer(topic: String) {
        val consumer: KafkaConsumer<String, Array<TopicKeys>> = KafkaConsumer(getConsumerDefaultProperties())
        consumer.subscribe(arrayListOf(topic))

        consumerExecutor.submit {
            while (true) {
                consumer.poll(Duration.ofSeconds(1)).forEach { record ->
                    utils.printRed("CONSUMER -> topic[${record.topic()}] - key[${record.key()}] - value[${record.value()}] - origin-topic[${String(record.headers().lastHeader("origin-topic").value())}]")
                }
            }
        }

    }

    fun getConsumerDefaultProperties(groupId: String = "test-consumer"): Properties {
        val props = Properties()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        return props
    }

    fun testInitializer() {
        val systemTopic: String = "SYSTEM_TOPIC"
        val inputTopicA: String = "input-topic-a"
        val inputTopicB: String = "input-topic-b"

        deleteTopic(systemTopic)
        deleteTopic(inputTopicA)
        deleteTopic(inputTopicB)
        deleteTopic("gateway-01-clients-topic")
        deleteTopic("gateway-01-keys-topic")
        deleteTopic("gateway-02-clients-topic")
        deleteTopic("gateway-02-keys-topic")
        createTopic(systemTopic,3,3)
        createTopic(inputTopicA,3,3)
        createTopic(inputTopicB,3,3)
        createTopic("gateway-01-clients-topic",3,3)
        createTopic("gateway-01-keys-topic",3,3)
        createTopic("gateway-02-clients-topic",3,3)
        createTopic("gateway-02-keys-topic",3,3)
    }
}