package com.isel.kafkastreamsmoduledemo.recordRouter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class Streams(
    private val recordRouterUtils: RecordRouterUtils,
    private val streamsStorage: StreamsStorage
) {

    companion object {
        private val SYSTEM_TOPIC = "SYSTEM_TOPIC"
        private val RECORD_ROUTER_SYSTEM_STREAM_ID = "RECORD_ROUTER_SYSTEM_STREAM"

    }
    private val mapper = jacksonObjectMapper()
    // Pair<Topic, Key>, List<GatewayTopic>
    private val gatewaysTopicKeys: ConcurrentHashMap<Pair<String, String>, List<String>> = ConcurrentHashMap<Pair<String, String>, List<String>>()
    // <Topic, List<GatewayTopic>> TODO: not yet used
    private val gatewaysFullTopics: ConcurrentHashMap<String, List<String>> = ConcurrentHashMap<String, List<String>>()
    private val gatewaysDetails: ConcurrentHashMap<String, GatewayDetails> = ConcurrentHashMap<String, GatewayDetails>()


    init {
        listenSystemTopic()
    }


    final fun listenSystemTopic(): KafkaStreams {

        val builder = StreamsBuilder()

        val inputStream: KStream<String, String> = builder.stream(SYSTEM_TOPIC, Consumed.with(Serdes.String(), Serdes.String()))

        inputStream.foreach { recordKey, jsonValue ->
            when(recordKey) {
                "new-gateway-key-topic" -> { // TODO: What if stream exists already and alike
                    val systemGatewayKeyTopic: SystemGatewayKeyTopic = mapper.readValue<SystemGatewayKeyTopic>(jsonValue)
                    createNewGatewayKeysStream(systemGatewayKeyTopic)
                    streamsStorage.restartAllDefaultRoutingStreams()
                }
                else -> {
                    println("listenSystemTopic - record followed 'else' route.")
                }
            }
        }

        val stream = KafkaStreams(builder.build(), recordRouterUtils.systemTopicStreamProperties(streamId = RECORD_ROUTER_SYSTEM_STREAM_ID))
        streamsStorage.setSystemTopicStream(stream) { listenSystemTopic() }
        stream.start()
        Runtime.getRuntime().addShutdownHook(Thread(stream::close))
        return stream
    }

    fun createNewGatewayKeysStream(systemGatewayKeyTopic: SystemGatewayKeyTopic): KafkaStreams {
        val builder = StreamsBuilder()

        val inputStream: KStream<String, String> =
            builder.stream(systemGatewayKeyTopic.keysTopicName, Consumed.with(Serdes.String(), Serdes.String()))

        inputStream.foreach { topic, keysJson ->
            updateRecordRoutingStreamsIfNeeded(topic, keysJson)
            val keys: List<String> = mapper.readValue<List<String>>(keysJson)
            val newKeys: MutableList<String> = mutableListOf()
            val toRemoveKeys: MutableList<String> = mutableListOf()
            gatewaysDetails.compute(systemGatewayKeyTopic.gatewayId) { _, gatewayDetails ->
                gatewayDetails?.topicsKeys?.compute(topic) { _, gatewayTopicKeys ->
                    val newGatewayTopicKeys = gatewayTopicKeys?: listOf()
                    for (key: String in newGatewayTopicKeys) { // TODO: instead of two separate fors, try with only one, or two (one inside the other).
                        if (!keys.contains(key)) {
                            toRemoveKeys.add(key)
                        }
                    }
                    for (key:String in keys) {
                        if (!newGatewayTopicKeys.contains(key)) {
                            newKeys.add(key)
                        }
                    }

                    keys
                }
                gatewayDetails
            }
            for (key: String in newKeys) {
                gatewaysTopicKeys.compute(Pair(topic, key)) { _, gatewayList ->
                    gatewayList?.plus(systemGatewayKeyTopic.clientTopicName) ?: listOf(systemGatewayKeyTopic.clientTopicName)
                }
            }

            for (key: String in toRemoveKeys) {
                gatewaysTopicKeys.compute(Pair(topic, key)) { _, gatewayList ->
                    gatewayList?.minus(systemGatewayKeyTopic.clientTopicName) ?: listOf()
                }
            }

        }

        val stream = KafkaStreams(builder.build(), recordRouterUtils.gatewayKeysStreamProperties(streamId = "${systemGatewayKeyTopic.keysTopicName}-stream"))
        streamsStorage.addGatewayKeysStream("${systemGatewayKeyTopic.keysTopicName}-stream", stream) { createNewGatewayKeysStream(systemGatewayKeyTopic) }

        stream.start()
        Runtime.getRuntime().addShutdownHook(Thread(stream::close))
        return stream

    }

    fun updateRecordRoutingStreamsIfNeeded(topic: String, keysJson: String) { // TODO: the keys are received to in the future close streams without keys at all
        if (gatewaysTopicKeys.keys().toList().find { it.first == topic } == null) {
            createTopicRecordRouterStream(topic, true)
        }
    }

    private fun createTopicRecordRouterStream(topic: String, firstTime: Boolean): KafkaStreams {
        val builder = StreamsBuilder()

        val inputStream: KStream<String, String> =
            builder.stream(topic, Consumed.with(Serdes.String(), Serdes.String()))

        for (gatewayEntry in gatewaysDetails) {
            inputStream.filter { key, _ -> isKeyForGateway(key, gatewayEntry.value.systemGatewayKeyTopic.clientTopicName, topic)}.to(gatewayEntry.value.systemGatewayKeyTopic.clientTopicName)
        }

        val stream = KafkaStreams(builder.build(),
            recordRouterUtils.topicRecordRouterStreamProperties(
                if(firstTime) "earliest" else "latest",
                "${topic}-topic-record-routing-stream"
            ))
        streamsStorage.addGatewayKeysStream("${topic}-topic-record-routing-stream", stream) { createTopicRecordRouterStream(topic, false) }

        stream.start()
        Runtime.getRuntime().addShutdownHook(Thread(stream::close))
        return stream

    }

    fun isKeyForGateway(key: String, gatewayClientTopicName: String, topic: String): Boolean {
        return gatewaysTopicKeys[Pair(topic, key)]?.contains(gatewayClientTopicName)?: false
    }


}