package com.isel.record.router.recordRouter

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

data class SystemHub(
    val hubId: String,
    val keysTopicName: String,
    val clientsTopicName: String,
    val commandsTopicName: String?
)

data class HubDetails (
    val hubId: String,
    val systemHub: SystemHub,
    val topicsKeys: ConcurrentHashMap<String, List<String>> = ConcurrentHashMap<String, List<String>>(), // TODO: Must disappear
    val fullTopics: AtomicReference<List<String>> = AtomicReference(listOf()) // TODO: Must disappear
)

data class HubTopicKeys(
    val keys: List<String>?,
    val allKeys: Boolean?
)

data class BooleanObj(
    var value: Boolean
)

data class TopicKeys(
    val topic: String,
    val keys: Array<Long>
) {
}