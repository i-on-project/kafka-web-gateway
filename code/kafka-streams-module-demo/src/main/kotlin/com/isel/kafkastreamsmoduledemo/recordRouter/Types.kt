package com.isel.kafkastreamsmoduledemo.recordRouter

import java.util.concurrent.ConcurrentHashMap

data class SystemGatewayKeyTopic(
    val gatewayId: String,
    val keysTopicName: String,
    val clientTopicName: String
)

data class GatewayDetails (
    val gatewayId: String,
    val systemGatewayKeyTopic: SystemGatewayKeyTopic,
    val topicsKeys: ConcurrentHashMap<String, List<String>> = ConcurrentHashMap<String, List<String>>()
)