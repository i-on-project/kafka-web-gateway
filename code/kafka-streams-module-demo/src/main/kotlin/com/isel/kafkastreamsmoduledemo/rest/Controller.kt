package com.isel.kafkastreamsmoduledemo.rest

import com.isel.kafkastreamsmoduledemo.kafkaStreams.KStreamsHandler
import org.apache.kafka.streams.Topology
import org.springframework.web.bind.annotation.*

@RestController
class Controller(private val streamsHandler: KStreamsHandler) {

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
}