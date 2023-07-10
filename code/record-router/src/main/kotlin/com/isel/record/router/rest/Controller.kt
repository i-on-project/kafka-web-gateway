package com.isel.record.router.rest

import com.isel.record.router.kafkaStreamsExperimentations.GlobalKTables
import com.isel.record.router.kafkaStreamsExperimentations.KStreamsHandler
import com.isel.record.router.kafkaStreamsExperimentations.UseCase
import com.isel.record.router.recordRouter.RRTesting
import com.isel.record.router.utilsExperimentations.KafkaStreamsUtils.Companion.DEFAULT_STREAM_ID
import org.springframework.web.bind.annotation.*

@RestController
class Controller(
    private val streamsHandler: KStreamsHandler,
    private val globalKTables: GlobalKTables,
    private val useCase: UseCase,
    private val rrTesting: RRTesting
) {

    @GetMapping("/test")
    fun testEndpoint(): String {
        rrTesting.test()
        return "yes, hello"
    }

    @PostMapping("/use-case-start")
    fun useCaseStart() {
        println("Starting use case")
        useCase.startStreamUsingStore("use-case-a")
        useCase.startStreamUsingStore("use-case-b")
        println("Ending use case")
    }

    @PostMapping("/use-case-stream-metrics")
    fun useCaseStreamMetrics() {

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