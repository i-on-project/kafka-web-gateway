package consumer;

import static com.isel.ps.KafkaClientsHelper.BOOTSTRAP_SERVERS;

import com.isel.ps.admin.KafkaAdminUtil;
import com.isel.ps.producer.KafkaProducerUtil;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.protocol.types.ArrayOf;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaConsumerUtilTest {
  private static final String TOPIC_NAME = "test-topic";
  private static final int PARTITION_COUNT = 3;
  private static final short REPLICATION_FACTOR = 1;
  private static final int RECORD_COUNT = 10;
  private final Properties DEFAULT_PROPERTIES = new Properties();
  private final KafkaProducerUtil<String, String> PRODUCER = new KafkaProducerUtil<>(BOOTSTRAP_SERVERS);
  private final KafkaAdminUtil ADMIN = new KafkaAdminUtil(BOOTSTRAP_SERVERS);

  private final ArrayList<KafkaConsumer> consumerPool = new ArrayList<>();
  private final ArrayList<String> topicPool = new ArrayList<>();

  @BeforeAll
  void setUp() {
    DEFAULT_PROPERTIES.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    DEFAULT_PROPERTIES.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    DEFAULT_PROPERTIES.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
  }

  @AfterAll
  void tearDown() {
    //kafkaTestHelper.tearDown();
    consumerPool.forEach(KafkaConsumer::close);
    ADMIN.deleteTopics(topicPool);
    PRODUCER.closeProducer();
    ADMIN.closeAdminClient();
  }

  /**
   * TODO: Still not working properly. Currently separating into smaller/simpler tests.
   */
  @Test
  void testConsumeRecords() throws InterruptedException, ExecutionException {
    String topicName = "testConsumeRecordsTopic";
    String groupId = "testConsumeRecordsGroup";

    createDefaultTopic(topicName);

    int key = 0;
    PRODUCER.sendRecord(topicName, 0, String.valueOf(key), String.valueOf(key)).get();
    ++key;

    KafkaConsumer<String, String> controlledConsumer1 = createControlledConsumer(groupId);
    KafkaConsumer<String, String> controlledConsumer2 = createControlledConsumer(groupId);
    KafkaConsumer<String, String> autoConsumer1 = createAutoConsumer(groupId);
    KafkaConsumer<String, String> autoConsumer2 = createAutoConsumer(groupId);

    TopicPartition partition1 = new TopicPartition(topicName, 0);

    autoConsumer1.subscribe(Collections.singleton(topicName));
    ConsumerRecords<String, String> autoRecords1 = autoConsumer1.poll(Duration.ofMillis(1000));
    PRODUCER.sendRecord(topicName, 0, String.valueOf(key), String.valueOf(key)).get();
    ++key;
    Thread.sleep(500);
    autoConsumer1.unsubscribe();

    autoConsumer2.subscribe(Collections.singleton(topicName));
    // autoConsumer2.assign(Collections.singleton(partition1));
    ConsumerRecords<String, String> autoRecords1_2 = autoConsumer2.poll(Duration.ofMillis(1000));
    //Thread.sleep(500);
    //autoConsumer2.pause(Collections.singleton(partition1));
    autoConsumer2.unsubscribe();

    controlledConsumer1.assign(Collections.singleton(partition1));
    controlledConsumer1.seek(partition1, 0);
    ConsumerRecords<String, String> controlledRecords1 = controlledConsumer1.poll(Duration.ofMillis(1000));
    //Thread.sleep(500);
    controlledConsumer1.pause(Collections.singleton(partition1));

    PRODUCER.sendRecord(topicName, 0, String.valueOf(key), "After Auto").get();

    controlledConsumer2.assign(Collections.singleton(partition1));
    controlledConsumer2.seek(partition1, 0);
    ConsumerRecords<String, String> controlledRecords2 = controlledConsumer2.poll(Duration.ofMillis(1000));
    Thread.sleep(500);
    controlledConsumer2.pause(Collections.singleton(partition1));

    controlledConsumer1.seek(partition1, 0);
    controlledConsumer1.resume(Collections.singleton(partition1));
    ConsumerRecords<String, String> controlledRecords3 = controlledConsumer1.poll(Duration.ofMillis(1000));
    Thread.sleep(500);
    controlledConsumer1.pause(Collections.singleton(partition1));

    autoConsumer1.resume(Collections.singleton(partition1));
    ConsumerRecords<String, String> autoRecords2 = autoConsumer1.poll(Duration.ofMillis(1000));
    Thread.sleep(500);
    autoConsumer1.pause(Collections.singleton(partition1));

    ADMIN.deleteTopic(topicName);
  }

  /**
   *  Demonstrates how after using pause (after the poll() usage) the poll() stops working
   *  and a call to poll() only works again after calling a resume().
   */
  @Test
  void testConsumerPauseAndResumePolling() throws ExecutionException, InterruptedException {
    String topicName = "testConsumerPauseAndResumePolling";

    createDefaultTopic(topicName);
    Thread.sleep(1000); // waiting for topic to be created TODO: use specific method in the future.

    int key = 0;
    PRODUCER.sendRecord(topicName, 0, String.valueOf(key), String.valueOf(key)).get();
    key++;

    KafkaConsumer<String, String> consumer = createControlledConsumer("testConsumerPauseAndResumePollingGroup");

    TopicPartition partition = new TopicPartition(topicName, 0);

    // First poll, normal. After polling, pauses it.
    consumer.assign(Collections.singleton(partition));
    consumer.seek(partition, 0);
    ConsumerRecords<String, String> records1 = consumer.poll(Duration.ofMillis(1000));
    consumer.pause(Collections.singleton(partition));

    assertEquals(1, records1.count());
    assertEquals("0", records1.records(partition).get(0).key());

    PRODUCER.sendRecord(topicName, 0, String.valueOf(key), String.valueOf(key)).get();
    key++;

    // Second polling, uses resume before calling the poll (it was paused previously).
    consumer.resume(Collections.singleton(partition));
    ConsumerRecords<String, String> records2 = consumer.poll(Duration.ofMillis(1000));
    consumer.pause(Collections.singleton(partition));

    assertEquals(1, records2.count());
    assertEquals("1", records2.records(partition).get(0).key());

    PRODUCER.sendRecord(topicName, 0, String.valueOf(key), String.valueOf(key)).get();

    // Third polling, doesn't use resume after it was paused, so the poll did nothing.
    ConsumerRecords<String, String> records3 = consumer.poll(Duration.ofMillis(1000));

    assertEquals(0, records3.count());

    consumer.close();
    ADMIN.deleteTopic(topicName);

  }
/*
  @Test
  void testCommitSync() {
    kafkaTestHelper.produceRecords(TOPIC_NAME, PARTITION_COUNT, RECORD_COUNT);
    KafkaConsumerUtil<String, String> consumerUtil =
        new KafkaConsumerUtil<>(consumerProps, TOPIC_NAME);

    consumerUtil.consumeRecords();
    consumerUtil.commitSync();

    consumerUtil.seekToBeginning();
    List<ConsumerRecord<String, String>> records = consumerUtil.consumeRecords();
    assertTrue(records.isEmpty());

    consumerUtil.close();
  }

  @Test
  void testCommitAsync() {
    kafkaTestHelper.produceRecords(TOPIC_NAME, PARTITION_COUNT, RECORD_COUNT);
    KafkaConsumerUtil<String, String> consumerUtil =
        new KafkaConsumerUtil<>(consumerProps, TOPIC_NAME);

    consumerUtil.consumeRecords();
    consumerUtil.commitAsync();

    consumerUtil.seekToBeginning();
    List<ConsumerRecord<String, String>> records = consumerUtil.consumeRecords();
    assertTrue(records.isEmpty());

    consumerUtil.close();
  }

  @Test
  void testSeekToBeginning() {
    kafkaTestHelper.produceRecords(TOPIC_NAME, PARTITION_COUNT, RECORD_COUNT);
    KafkaConsumerUtil<String, String> consumerUtil =
        new KafkaConsumerUtil<>(consumerProps, TOPIC_NAME);

    consumerUtil.seekToBeginning();
    List<ConsumerRecord<String, String>> records = consumerUtil.consumeRecords();
    assertEquals(PARTITION_COUNT * RECORD_COUNT, records.size());

    consumerUtil.close();
  }

  @Test
  void testSeekToEnd() {
    kafkaTestHelper.produceRecords(TOPIC_NAME, PARTITION_COUNT, RECORD_COUNT);
    KafkaConsumerUtil<String, String> consumerUtil =
        new KafkaConsumerUtil<>(consumerProps, TOPIC_NAME);

    consumerUtil.seekToEnd();
    List<ConsumerRecord<String, String>> records = consumerUtil.consumeRecords();
    assertTrue(records.isEmpty());

    consumerUtil.close();
  }

  @Test
  void testSeek() {
    kafkaTestHelper.produceRecords(TOPIC_NAME, PARTITION_COUNT, RECORD_COUNT);
    KafkaConsumerUtil<String, String> consumerUtil =
        new KafkaConsumerUtil<>(consumerProps, TOPIC_NAME);

    consumerUtil.seek(new TopicPartition(TOPIC_NAME, 0), RECORD_COUNT - 1);
    List<ConsumerRecord<String, String>> records = consumerUtil.consumeRecords();
    assertEquals(PARTITION_COUNT, records.size());

    consumerUtil.close();
  }

  @Test
  void testAssign() {
    kafkaTestHelper.produceRecords(TOPIC_NAME, PARTITION_COUNT, RECORD_COUNT);
    KafkaConsumerUtil<String, String> consumerUtil = new KafkaConsumerUtil<>(consumerProps);

    consumerUtil.assign(
        Arrays.asList(new TopicPartition(TOPIC_NAME, 0), new TopicPartition(TOPIC_NAME, 1)));
    List<ConsumerRecord<String, String>> records = consumerUtil.consumeRecords();
    assertEquals(2 * RECORD_COUNT, records.size());

    consumerUtil.close();
  }

  @Test
  void testUnassign() {
    kafkaTestHelper.produceRecords(TOPIC_NAME, PARTITION_COUNT, RECORD_COUNT);
    KafkaConsumerUtil<String, String> consumerUtil =
        new KafkaConsumerUtil<>(consumerProps, TOPIC_NAME);

    consumerUtil.consumeRecords();
    consumerUtil.unassign();

    consumerUtil.seekToBeginning();
    List<ConsumerRecord<String, String>> records = consumerUtil.consumeRecords();
    assertEquals(PARTITION_COUNT * RECORD_COUNT, records.size());

    consumerUtil.close();
  }

  @Test
  void testPollWithTimeout() {
    kafkaTestHelper.produceRecords(TOPIC_NAME, PARTITION_COUNT, RECORD_COUNT);
    KafkaConsumerUtil<String, String> consumerUtil =
        new KafkaConsumerUtil<>(consumerProps, TOPIC_NAME);

    List<ConsumerRecord<String, String>> records = consumerUtil.poll(5000);
    assertEquals(PARTITION_COUNT * RECORD_COUNT, records.size());

    consumerUtil.close();
  }

  @Test
  void testPollWithoutTimeout() {
    kafkaTestHelper.produceRecords(TOPIC_NAME, PARTITION_COUNT, RECORD_COUNT);
    KafkaConsumerUtil<String, String> consumerUtil =
        new KafkaConsumerUtil<>(consumerProps, TOPIC_NAME);

    List<ConsumerRecord<String, String>> records = consumerUtil.poll();
    assertEquals(PARTITION_COUNT * RECORD_COUNT, records.size());

    consumerUtil.close();
  }

  @Test
  void testPauseResume() {
    kafkaTestHelper.produceRecords(TOPIC_NAME, PARTITION_COUNT, RECORD_COUNT);
    KafkaConsumerUtil<String, String> consumerUtil =
        new KafkaConsumerUtil<>(consumerProps, TOPIC_NAME);

    consumerUtil.pause();
    List<ConsumerRecord<String, String>> records = consumerUtil.poll();
    assertTrue(records.isEmpty());

    consumerUtil.resume();
    records = consumerUtil.consumeRecords();
    assertEquals(PARTITION_COUNT * RECORD_COUNT, records.size());

    consumerUtil.close();
  }

 */

  private void createDefaultTopic(String topicName) throws ExecutionException, InterruptedException {
    ADMIN.createTopic(topicName, 1, REPLICATION_FACTOR);
    topicPool.add(topicName);
  }

  private KafkaConsumer<String, String> createAutoConsumer(String groupId) {
    Properties props = new Properties();
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
    return createConsumer(groupId, props);
  }
  private KafkaConsumer<String, String> createControlledConsumer(String groupId) {
    return createConsumer(groupId, new Properties());
  }
  private KafkaConsumer<String, String> createConsumer(String groupId, Properties props) {
    props.putAll((Properties) DEFAULT_PROPERTIES.clone());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
    // Adding all the created consumers to a pool so that no consumer ends up not being closed.
    consumerPool.add(consumer);
    return consumer;
  }
}
