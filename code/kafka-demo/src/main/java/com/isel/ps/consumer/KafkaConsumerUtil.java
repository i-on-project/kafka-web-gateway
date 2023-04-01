package com.isel.ps.consumer;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

public class KafkaConsumerUtil<K, V> {
  private final Properties properties;
  private final String topicName;
  private final AtomicBoolean closed = new AtomicBoolean(false);
  private final Thread mainThread;

  private KafkaConsumer<K, V> consumer;

  public KafkaConsumerUtil(Properties properties, String topicName) {
    this.properties = properties;
    this.topicName = topicName;
    mainThread = Thread.currentThread();
    startConsumer();
  }

  private void startConsumer() {
    consumer = new KafkaConsumer<>(properties);
    consumer.subscribe(
        Collections.singletonList(topicName),
        new ConsumerRebalanceListener() {
          @Override
          public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            // No-op
          }

          @Override
          public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            // Reset consumer offsets to the beginning of the topic partitions
            consumer.seekToBeginning(partitions);
          }
        });
  }

  public List<ConsumerRecord<K, V>> consumeRecords() {
    List<ConsumerRecord<K, V>> records = new ArrayList<>();
    try {
      while (!closed.get()) {
        // TODO consumer.poll();
        ConsumerRecords<K, V> consumerRecords = consumer.poll(Duration.ofSeconds(1));
        consumerRecords.forEach(records::add);
      }
    } catch (WakeupException e) {
      // Ignore
    } finally {
      consumer.close();
    }
    return records;
  }

  public void commitSync() {
    consumer.commitSync();
  }

  public void commitAsync() {
    consumer.commitAsync();
  }

  public void seekToBeginning() {
    consumer.seekToBeginning(consumer.assignment());
  }

  public void seekToEnd() {
    consumer.seekToEnd(consumer.assignment());
  }

  public void seek(TopicPartition partition, long offset) {
    consumer.seek(partition, offset);
  }

  public void pause(Collection<TopicPartition> partitions) {
    consumer.pause(partitions);
  }

  public void resume(Collection<TopicPartition> partitions) {
    consumer.resume(partitions);
  }

  public void subscribe(List<String> topics) {
    consumer.subscribe(topics);
  }

  public void assign(Collection<TopicPartition> partitions) {
    consumer.assign(partitions);
  }

  public void unsubscribe() {
    consumer.unsubscribe();
  }

  public void close() {
    closed.set(true);
    consumer.wakeup();
    try {
      mainThread.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
