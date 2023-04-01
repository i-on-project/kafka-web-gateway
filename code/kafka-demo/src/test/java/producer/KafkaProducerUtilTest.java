package producer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.isel.ps.producer.KafkaProducerUtil;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class KafkaProducerUtilTest {
  private static final String TOPIC_NAME = "test_topic";
  private static final String BOOTSTRAP_SERVERS = "localhost:29092";
  private static final String MESSAGE_KEY = "test_key";
  private static final String MESSAGE_VALUE = "test_value";

  private KafkaConsumer<String, String> consumer;
  private Properties consumerProperties;
  private KafkaProducerUtil<String, String> producer;

  @BeforeEach
  public void setUp() {
    consumerProperties = new Properties();
    consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "test_group");
    consumerProperties.put(
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    consumerProperties.put(
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    consumerProperties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
    consumer = new KafkaConsumer<>(consumerProperties);
    consumer.subscribe(Collections.singleton(TOPIC_NAME));
    producer = new KafkaProducerUtil<>(BOOTSTRAP_SERVERS, TOPIC_NAME);
  }

  @AfterEach
  public void tearDown() {
    producer.closeProducer();
    consumer.close();
  }

  @Test
  public void testSendRecord() throws ExecutionException, InterruptedException {
    Future<RecordMetadata> futureMetadata = producer.sendRecord(MESSAGE_KEY, MESSAGE_VALUE);
    RecordMetadata metadata = futureMetadata.get();
    assertEquals(TOPIC_NAME, metadata.topic());
    assertTrue(metadata.hasOffset());
    assertTrue(metadata.hasTimestamp());
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
    assertEquals(1, records.count());
    ConsumerRecord<String, String> record = records.iterator().next();
    assertEquals(MESSAGE_KEY, record.key());
    assertEquals(MESSAGE_VALUE, record.value());
  }
}
