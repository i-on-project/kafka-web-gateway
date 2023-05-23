package com.isel.kafkastreamsmoduledemo.rest

import com.isel.kafkastreamsmoduledemo.kafkaStreams.GlobalKTables
import com.isel.kafkastreamsmoduledemo.kafkaStreams.KStreamsHandler
import com.isel.kafkastreamsmoduledemo.utiils.KafkaStreamsUtils.Companion.DEFAULT_STREAM_ID
import org.apache.kafka.streams.Topology
import org.springframework.web.bind.annotation.*
import java.lang.Long
import java.lang.Long.parseLong

@RestController
class Controller(private val streamsHandler: KStreamsHandler, private val globalKTables: GlobalKTables) {

    @GetMapping("/test")
    fun testEndpoint(): String {
        println("this is the controller")
        return "yes, hello"
    }

    @PostMapping("/start-stream-evens/{id}")
    fun startStreamEvens(@PathVariable id: String): String {
        return streamsHandler.startStreaming(id)

    }

    @PostMapping("/close-stream/{id}")
    fun closeStream(@PathVariable id: String): String {
        return streamsHandler.closeStream(id)
    }

    @PostMapping("/logger-stream")
    fun loggerStream() {
        streamsHandler.loggerStream()
    }

    @PostMapping("/logger-consumer")
    fun loggerConsumer() {
        streamsHandler.loggerConsumer()
    }

    @PostMapping("/stream-for-topic/{topicId}")
    fun streamForTopic(@PathVariable topicId: String) {
        println("POST streams for topic with id:[$topicId]")
        streamsHandler.newStreamForTopic(topicId, DEFAULT_STREAM_ID)
    }

    @PostMapping("/add-new-key")
    fun addNewKey(@RequestParam gateway: String) {
        println("add new key endpoint with gateway[$gateway]")
        streamsHandler.insertToGatewayKeys(gateway)
    }

    @PostMapping("/add-new-key-to-gateway01")
    fun addNewKeyToGateway01() {
        streamsHandler.addNewKeyToGateway01()
    }

    @PostMapping("/start-global-table")
    fun startGlobalTable() {
        globalKTables.globalTable()
    }

    @PostMapping("/access-global-table")
    fun accessGlobalTable() {
        globalKTables.accessGlobalTable()
    }

    @PostMapping("/populate-global-table-topic")
    fun populateGlobalTableTopic() {
        globalKTables.populateGlobalTable()
    }

    @PostMapping("/consume-global-table-topics")
    fun consumeGlobalTableTopics() {
        globalKTables.consumeSaidTablesTopic()
    }

    @PostMapping("/print-global-table")
    fun printGlobalTable() {
        globalKTables.printGlobalTable()
    }

}