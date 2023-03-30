package com.isel.ps.kafka_clients;

import com.isel.ps.Constants;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.TopicConfig;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AdminTools {

    //private static final Logger log = LogManager.getLogger(AdminTools.class);
    private final Admin admin;


    public AdminTools() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.KAFKA_URL);

        this.admin = Admin.create(props);
    }

    /**
     * Waiting for answer of topic being created might not be ideal because creating a topic might take a while,
     * more so depending on the replication factor, so to make sure topic was created, another request should be
     * done to get the topic.
     * @param topicName
     * @param partitions
     * @param replicationFactor
     */
    public void createTopic(String topicName, int partitions, short replicationFactor) {
        try {
            CreateTopicsResult result = admin.createTopics(Collections.singleton(
                    new NewTopic(topicName, partitions, replicationFactor)
                            .configs(Collections.singletonMap(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT))
            ));
            result.all().get();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public KafkaFuture<Set<String>> getAllTopics() {
        try {
            ListTopicsResult result =  admin.listTopics(new ListTopicsOptions());


            return result.names();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {

        /*
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
         */

    }

}
