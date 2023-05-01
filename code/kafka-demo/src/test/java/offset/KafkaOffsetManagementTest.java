package offset;

import java.util.*;
import org.apache.kafka.clients.consumer.*;

/*
public class KafkaOffsetManagementTest {
  private static final String BOOTSTRAP_SERVERS = "localhost:29092";
  private static final String TOPIC_NAME = "test-topic";

  private static final int PARTITION = 0;
  private static final int NUM_CONSUMERS = 3;

  private List<Consumer<String, String>> consumers;

  private Consumer<String, String> consumer;

  @BeforeEach
  public void setUp() {
    Properties properties = new Properties();
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    properties.setProperty(
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    consumers = new ArrayList<>();
    for (int i = 0; i < NUM_CONSUMERS; i++) {
      properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group-" + i);
      Consumer<String, String> consumer = new KafkaConsumer<>(properties);
      consumers.add(consumer);
    }
  }

  @AfterEach
  public void tearDown() {
    for (Consumer<String, String> consumer : consumers) {
      consumer.close();
    }
  }

  @Test
  public void testConsumeFromStart() {
    consumer.subscribe(Collections.singleton(TOPIC_NAME));
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
    assertFalse(records.isEmpty());

    for (ConsumerRecord<String, String> record : records) {
      assertEquals(0L, record.offset());
    }
  }

  @Test
  public void testConsumeFromOffset() {
    consumer.assign(Collections.singleton(new TopicPartition(TOPIC_NAME, PARTITION)));
    consumer.seek(new TopicPartition(TOPIC_NAME, PARTITION), 5L);
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
    assertFalse(records.isEmpty());

    for (ConsumerRecord<String, String> record : records) {
      assertEquals(5L, record.offset());
    }
  }

  @Test
  public void testConsumeWithMultipleConsumers() {
    // Create topic
    Map<TopicPartition, Long> partitionOffsets = new HashMap<>();
    partitionOffsets.put(new TopicPartition(TOPIC_NAME, PARTITION), 0L);
    KafkaTestHelper.createTopic(TOPIC_NAME, partitionOffsets, BOOTSTRAP_SERVERS);

    // Produce messages
    List<String> messages =
        Arrays.asList("message1", "message2", "message3", "message4", "message5");
    KafkaTestHelper.produceMessages(TOPIC_NAME, messages, BOOTSTRAP_SERVERS);

    // Start consuming messages
    List<ConsumerRecords<String, String>> recordsList = new ArrayList<>();
    for (Consumer<String, String> consumer : consumers) {
      consumer.subscribe(Collections.singleton(TOPIC_NAME));
      ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
      assertFalse(records.isEmpty());
      recordsList.add(records);
    }

    // Check that each consumer starts consuming messages from the beginning
    for (ConsumerRecords<String, String> records : recordsList) {
      for (ConsumerRecord<String, String> record : records) {
        assertEquals(0L, record.offset());
      }
    }

    // Commit the offset of the first message for each consumer
    for (int i = 0; i < NUM_CONSUMERS; i++) {
      Consumer<String, String> consumer = consumers.get(i);
      for (ConsumerRecord<String, String> record : recordsList.get(i)) {
        TopicPartition tp = new TopicPartition(record.topic(), record.partition());
        OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(record.offset() + 1);
        consumer.commitSync(Collections.singletonMap(tp, offsetAndMetadata));
      }
    }

    // Create a new consumer and verify it starts consuming from the correct offset
    Properties properties = new Properties();
    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "test-group-1");
    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
    consumer.subscribe(Collections.singleton(TOPIC_NAME));
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
    assertFalse(records.isEmpty());
    for (ConsumerRecord<String, String> record : records) {
      assertEquals(1L, record.offset());
    }
    consumer.close();
  }

  @Test
  void testConsumeWithMultipleConsumers() throws InterruptedException {
    // create Kafka test topic
    String topic = "test-topic";
    KafkaTestHelper.createTopic(topic, 2, (short) 1);

    // create Kafka producer
    KafkaProducer<String, String> producer = producerUtil.createProducer();

    // produce messages to Kafka topic
    int numMessages = 10;
    for (int i = 0; i < numMessages; i++) {
      String message = "Message " + i;
      producer.send(new ProducerRecord<>(topic, message));
    }

    // create Kafka consumers
    int numConsumers = 3;
    List<KafkaConsumer<String, String>> consumers = new ArrayList<>();
    for (int i = 0; i < numConsumers; i++) {
      KafkaConsumer<String, String> consumer = consumerUtil.createConsumer("test-group");
      consumers.add(consumer);
    }

    // start consumers and subscribe to topic
    for (KafkaConsumer<String, String> consumer : consumers) {
      consumer.subscribe(Collections.singleton(topic));
      consumer.poll(Duration.ofMillis(1000)); // consume any initial messages
    }

    // assert that each consumer has consumed all messages
    for (KafkaConsumer<String, String> consumer : consumers) {
      int messagesConsumed = 0;
      while (messagesConsumed < numMessages) {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
        for (ConsumerRecord<String, String> record : records) {
          messagesConsumed++;
        }
      }
      assertEquals(numMessages, messagesConsumed);
    }
  }
}

 */
