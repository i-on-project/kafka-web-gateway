package com.isel.ps.kafka_clients;

import com.isel.ps.Constants;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class AdminTools {

    private static final Logger log = LogManager.getLogger(AdminTools.class);
    private Admin admin;


    public AdminTools() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.KAFKA_URL);

        this.admin = Admin.create(props);
    }

    public KafkaFuture<Void> createTopic(String topicName, int partitions, short replicationFactor) {
        try {
            CreateTopicsResult result = admin.createTopics(Collections.singleton(
                    new NewTopic(topicName, partitions, replicationFactor)
                            .configs(Collections.singletonMap(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT))
            ));


            return result.values().get(topicName);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean getAllTopics() {

    }

    public static void main(String[] args) {

        try {
            String topicName = "demo-topic";
            int partitions = 5;
            short replicationFactor = 3;
            // Create a compacted topic
            CreateTopicsResult result = admin.createTopics(Collections.singleton(
                    new NewTopic(topicName, partitions, replicationFactor)
                            .configs(Collections.singletonMap(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT))));

            // Call values() to get the result for a specific topic
            KafkaFuture<Void> future = result.values().get(topicName);

            // Call get() to block until the topic creation is complete or has failed
            // if creation failed the ExecutionException wraps the underlying cause.
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}
