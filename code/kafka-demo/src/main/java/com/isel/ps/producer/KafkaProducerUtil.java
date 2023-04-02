package com.isel.ps.producer;

import java.util.Properties;
import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaProducerUtil<K, V> {
  private final Producer<K, V> producer;
  private String topicName = "default";

  public KafkaProducerUtil(String bootstrapServers, String topicName) {
    this.topicName = topicName;
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    producer = new KafkaProducer<>(props);
  }

  public KafkaProducerUtil(String bootstrapServers) {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    producer = new KafkaProducer<>(props);
  }

  public Future<RecordMetadata> sendRecord(K key, V value) {
    ProducerRecord<K, V> record = new ProducerRecord<>(topicName, key, value);
    return producer.send(record);
  }

  public Future<RecordMetadata> sendRecord(String topic, K key, V value) {
    ProducerRecord<K, V> record = new ProducerRecord<>(topic, key, value);
    return producer.send(record);
  }

  public Future<RecordMetadata> sendRecord(String topic, int partition, K key, V value) {
    ProducerRecord<K, V> record = new ProducerRecord<>(topic, partition, key, value);
    return producer.send(record);
  }

  public void closeProducer() {
    producer.close();
  }
}
