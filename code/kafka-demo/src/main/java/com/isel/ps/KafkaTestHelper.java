package com.isel.ps;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaTestHelper {

  private final String bootstrapServers;
  private final int partitions;
  private final int replicationFactor;

  public KafkaTestHelper(String bootstrapServers, int partitions, int replicationFactor) {
    this.bootstrapServers = bootstrapServers;
    this.partitions = partitions;
    this.replicationFactor = replicationFactor;
  }

  public void createTopic(String topicName) {
    Properties props = new Properties();
    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    try (AdminClient adminClient = AdminClient.create(props)) {
      NewTopic topic = new NewTopic(topicName, partitions, (short) replicationFactor);
      adminClient.createTopics(Collections.singleton(topic)).all().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  public void deleteTopic(String topicName) {
    Properties props = new Properties();
    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    try (AdminClient adminClient = AdminClient.create(props)) {
      adminClient.deleteTopics(Collections.singleton(topicName)).all().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  public void produceRecords(String topicName, int partitionCount, int recordCount) {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
      for (int i = 0; i < partitionCount; i++) {
        for (int j = 0; j < recordCount; j++) {
          String key = "key_" + i + "_" + j;
          String value = "value_" + i + "_" + j;
          ProducerRecord<String, String> record = new ProducerRecord<>(topicName, i, key, value);
          producer.send(record);
        }
      }
      producer.flush();
    }
  }
}
