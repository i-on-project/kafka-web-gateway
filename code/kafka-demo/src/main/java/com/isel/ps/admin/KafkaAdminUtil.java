package com.isel.ps.admin;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;

public class KafkaAdminUtil {
  private final Admin adminClient;

  ExecutorService executor = Executors.newSingleThreadExecutor();

  public KafkaAdminUtil(String bootstrapServers) {
    final Map<String, Object> adminConfig = new HashMap<>();
    adminConfig.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    this.adminClient = Admin.create(adminConfig);
  }

  public void createTopic(String topicName, int numPartitions, short replicationFactor)
      throws InterruptedException, ExecutionException {
    final NewTopic newTopic = new NewTopic(topicName, numPartitions, replicationFactor);
    final CreateTopicsOptions options = new CreateTopicsOptions().timeoutMs(5000);
    try {
      adminClient.createTopics(Collections.singleton(newTopic), options).all().get();
    } catch (ExecutionException e) {
      if (e.getCause() instanceof TopicExistsException) {
        System.out.println("Topic " + topicName + " already exists");
      } else {
        throw e;
      }
    }
  }

  public void deleteTopic(String topicName) throws InterruptedException, ExecutionException {
    try {
      final DeleteTopicsOptions options = new DeleteTopicsOptions().timeoutMs(5000);
      adminClient.deleteTopics(Collections.singleton(topicName), options).all().get();
    } catch (ExecutionException e) {
      if (e.getCause() instanceof UnknownTopicOrPartitionException) {
        System.out.println("Topic " + topicName + " does not exist");
      } else {
        throw e;
      }
    }
  }

  public DeleteTopicsResult deleteTopics(Collection<String> topics) {
    final DeleteTopicsOptions options = new DeleteTopicsOptions().timeoutMs(5000);
    return adminClient.deleteTopics(topics, options);
  }

  public TopicDescription describeTopic(String topicName)
      throws InterruptedException, ExecutionException {
    final DescribeTopicsOptions options = new DescribeTopicsOptions().timeoutMs(5000);
    final DescribeTopicsResult result =
        adminClient.describeTopics(Collections.singleton(topicName), options);
    final Map<String, TopicDescription> topicDescriptions = result.allTopicNames().get();
    if (topicDescriptions.containsKey(topicName)) {
      return topicDescriptions.get(topicName);
    } else {
      throw new ExecutionException(
          new UnknownTopicOrPartitionException("Topic " + topicName + " does not exist"));
    }
  }

  public Set<String> listTopics() throws InterruptedException, ExecutionException {
    final ListTopicsOptions options = new ListTopicsOptions().timeoutMs(5000);
    final ListTopicsResult result = adminClient.listTopics(options);
    return result.names().get();
  }

  public boolean hasTopic(String topicName) throws ExecutionException, InterruptedException {
    final DescribeTopicsOptions options = new DescribeTopicsOptions().timeoutMs(5000);
    final DescribeTopicsResult result =
        adminClient.describeTopics(Collections.singleton(topicName), options);
    final Map<String, TopicDescription> topicDescriptions = result.allTopicNames().get();
    return topicDescriptions.containsKey(topicName);
  }

  public Future<Boolean> waitTopicCreation(String topicName) {
    return executor.submit(() -> {
      int tries = 0;
      while(!hasTopic(topicName) && tries < 20 ) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        tries++;
      }
      return tries < 20;
    });

  }

  public void closeAdminClient() {
    adminClient.close();
  }
}
