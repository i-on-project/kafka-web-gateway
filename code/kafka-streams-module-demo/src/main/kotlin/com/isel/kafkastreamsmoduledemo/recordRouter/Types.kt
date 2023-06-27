package com.isel.kafkastreamsmoduledemo.recordRouter

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

data class SystemGateway(
    val gatewayId: String,
    val keysTopicName: String,
    val clientsTopicName: String,
    val commandsTopicName: String?
)

data class GatewayDetails (
    val gatewayId: String,
    val systemGateway: SystemGateway,
    val topicsKeys: ConcurrentHashMap<String, List<String>> = ConcurrentHashMap<String, List<String>>(), // TODO: Must disappear
    val fullTopics: AtomicReference<List<String>> = AtomicReference(listOf()) // TODO: Must disappear
)

data class GatewayTopicKeys(
    val keys: List<String>?,
    val allKeys: Boolean?
)

data class BooleanObj(
    var value: Boolean
)