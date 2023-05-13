package com.isel.kafkastreamsmoduledemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KafkaStreamsModuleDemoApplication

fun main(args: Array<String>) {
	runApplication<KafkaStreamsModuleDemoApplication>(*args)
}
