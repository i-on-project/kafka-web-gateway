package consumer;

import static com.isel.ps.KafkaClientsHelper.BOOTSTRAP_SERVERS;

import com.isel.ps.admin.KafkaAdminUtil;
import com.isel.ps.producer.KafkaProducerUtil;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaConsumerTest {
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
   * This is a manual test, while the test runs the brokers should reset one by one.
   */
  @Test
  void simpleConsumerAndProducerWhileBrokersFail() {
    // TODO:
  }



  /**
   *  When only subscribe is used (no seek is used) and there are no committed records yet then the only pulled records are the ones published after starting poll().
   */
  @Test
  void AutomaticConsumerSubscribeNoCommittedRecords() throws ExecutionException, InterruptedException {
    String topicName = "AutomaticConsumerSubscribeNoCommittedRecordsTopic";
    String groupId = "AutomaticConsumerSubscribeNoCommittedRecordsGroup";

    int key = 0;
    PRODUCER.sendRecord(topicName, 0, String.valueOf(key), String.valueOf(key)).get();
    ++key;
    PRODUCER.sendRecord(topicName, 0, String.valueOf(key), String.valueOf(key)).get();
    ++key;

    KafkaConsumer<String, String> consumer = createAutoConsumer(groupId);
    createDefaultTopic(topicName);
    TopicPartition partition = new TopicPartition(topicName, 0);

    consumer.subscribe(Collections.singleton(topicName));
    ConsumerRecords<String, String> records1 = consumer.poll(Duration.ofMillis(2000));
    consumer.pause(Collections.singleton(partition));

    assertEquals(0, records1.count());
  }

  /**
   *  For subscribe() to make the poll() fetch older records (with offsets previous to the poll() call),
   *  some record needs to already be signaled as committed. For that, a consumer was used with assign()
   *  instead of subscribe() so that the seek() can be used to manually choose an offset to fetch.
   *  After that, the commitSync() was used to signal the fetched record as committed so that a later consumer
   *  can use subscribe to continue fetching from the last committed record instead of only fetching the records
   *  being published during the poll() call.
   */
  @Test
  void AutomaticConsumerSubscribeCommittedRecords() throws ExecutionException, InterruptedException {
    String topicName = "AutomaticConsumerSubscribeCommittedRecordsTopic";
    String groupId = "AutomaticConsumerSubscribeCommittedRecordsGroup";

    int key = 0;
    PRODUCER.sendRecord(topicName, 0, String.valueOf(key), String.valueOf(key)).get();

    KafkaConsumer<String, String> autoConsumer = createAutoConsumer(groupId);
    KafkaConsumer<String, String> controlledConsumer = createManualConsumer(groupId);
    createDefaultTopic(topicName);
    TopicPartition partition = new TopicPartition(topicName, 0);

    controlledConsumer.assign(Collections.singleton(partition));
    controlledConsumer.seek(partition, 0);
    ConsumerRecords<String, String>  records1 = controlledConsumer.poll(Duration.ofMillis(200));
    controlledConsumer.commitSync();
    controlledConsumer.unsubscribe();

    assertEquals(1, records1.count());
    assertEquals(String.valueOf(key), records1.records(partition).get(0).key());

    key++;
    PRODUCER.sendRecord(topicName, 0, String.valueOf(key), String.valueOf(key)).get();

    autoConsumer.subscribe(Arrays.asList(topicName));
    ConsumerRecords<String, String> records2 = autoConsumer.poll(Duration.ofMillis(200));

    assertEquals(1, records2.count());
    assertEquals(String.valueOf(key), records2.records(partition).get(0).key());
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

    KafkaConsumer<String, String> controlledConsumer1 = createManualConsumer(groupId);
    KafkaConsumer<String, String> controlledConsumer2 = createManualConsumer(groupId);
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

    KafkaConsumer<String, String> consumer = createManualConsumer("testConsumerPauseAndResumePollingGroup");

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
  }

  @Test
  void testSeekEditOffset() throws ExecutionException, InterruptedException {
    String topicName = "testSeekEditOffsetTopic";
    String groupId = "testSeekEditOffsetGroup";

    createDefaultTopic(topicName);
    int key = 0;
    PRODUCER.sendRecord(topicName, 0, String.valueOf(key), String.valueOf(key)).get();
    key++;
    PRODUCER.sendRecord(topicName, 0, String.valueOf(key), String.valueOf(key)).get();

    KafkaConsumer<String, String> consumer = createManualConsumer(groupId);
    TopicPartition partition = new TopicPartition(topicName, 0);

    consumer.assign(Arrays.asList(partition));
    consumer.seek(partition, 0);

    ConsumerRecords<String, String>  records1 = consumer.poll(Duration.ofMillis(200));
    consumer.commitSync();                                            //TODO: This commit was needed for some reason
    assertEquals(2, records1.count());

    consumer.seek(partition, 1);
    ConsumerRecords<String, String>  records2 = consumer.poll(Duration.ofMillis(200));
    assertEquals(1, records2.count());
  }

  @Test
  void testSeekToEndAndSeekToBeginning() throws ExecutionException, InterruptedException {
    String topicName = "testSeekToEndAndSeekToBeginningTopic";
    String groupId = "testSeekToEndAndSeekToBeginningGroup";

    createDefaultTopic(topicName);
    int key = 0;
    PRODUCER.sendRecord(topicName, 0, String.valueOf(key), String.valueOf(key)).get();
    key++;
    PRODUCER.sendRecord(topicName, 0, String.valueOf(key), String.valueOf(key)).get();

    KafkaConsumer<String, String> consumer = createManualConsumer(groupId);
    TopicPartition partition = new TopicPartition(topicName, 0);
    consumer.assign(Arrays.asList(partition));

    consumer.seekToBeginning(Collections.singleton(partition));
    ConsumerRecords<String, String>  records1 = consumer.poll(Duration.ofMillis(200));
    consumer.commitSync();
    assertEquals(2, records1.count());

    consumer.seekToBeginning(Collections.singleton(partition));
    ConsumerRecords<String, String>  records2 = consumer.poll(Duration.ofMillis(200));
    consumer.commitSync();
    assertEquals(2, records2.count());

    consumer.seekToEnd(Collections.singleton(partition));
    ConsumerRecords<String, String>  records3 = consumer.poll(Duration.ofMillis(200));
    consumer.commitSync();
    assertEquals(0, records3.count());

    consumer.seekToBeginning(Collections.singleton(partition));
    ConsumerRecords<String, String>  records4 = consumer.poll(Duration.ofMillis(200));
    ConsumerRecords<String, String>  records5 = consumer.poll(Duration.ofMillis(200));
    ConsumerRecords<String, String>  records6 = consumer.poll(Duration.ofMillis(200));
    ConsumerRecords<String, String>  records7 = consumer.poll(Duration.ofMillis(200));
    consumer.commitSync();
    assertEquals(0, records4.count());
    assertEquals(2, records5.count() + records6.count() + records7.count());
  }

  /**
   * When partitions are manually assigned to poll, in each poll call its unpredictable if only
   * one partition's records are requested or if both/all are...
   * TODO: Due to discovered complexity, this test is not working and has been left behind... (for now)
   */
  //@Test
  void testPauseAndResumeChosenTopicPartitions() throws ExecutionException, InterruptedException {
    String topicName = "testPauseAndResumeChosenTopicPartitionsTopic";
    String groupId = "testPauseAndResumeChosenTopicPartitionsGroup";

    createDefaultTopic(topicName, 2);

    int key = 0;
    PRODUCER.sendRecord(topicName, 0, String.valueOf(key), String.valueOf(key)).get();
    key++;
    PRODUCER.sendRecord(topicName, 1, String.valueOf(key), String.valueOf(key)).get();

    KafkaConsumer<String, String> consumer = createManualConsumer(groupId);
    TopicPartition partition0 = new TopicPartition(topicName, 0);
    TopicPartition partition1 = new TopicPartition(topicName, 1);

    consumer.assign(List.of(partition0, partition1));
    consumer.seek(partition0, 0);
    consumer.seek(partition1, 0);

    ConsumerRecords<String, String>  records1_partition0 = consumer.poll(Duration.ofMillis(200));
    assertEquals(1, records1_partition0.count());
    assertEquals("0", records1_partition0.records(partition0).get(0).value());
    assertEquals("0", records1_partition0.iterator().next().value());

    ConsumerRecords<String, String>  records1_partition1 = consumer.poll(Duration.ofMillis(200));
    assertEquals(1, records1_partition1.count());
    assertEquals("1", records1_partition1.records(partition1).get(0).value());
    assertEquals("1", records1_partition1.iterator().next().value());

    key++;
    PRODUCER.sendRecord(topicName, 0, String.valueOf(key), String.valueOf(key)).get();
    key++;
    PRODUCER.sendRecord(topicName, 1, String.valueOf(key), String.valueOf(key)).get();

    ConsumerRecords<String, String>  records2_partition0 = consumer.poll(Duration.ofMillis(200));
    assertEquals(1, records2_partition0.count());
    assertEquals("2", records2_partition0.records(partition0).get(0).value());

    ConsumerRecords<String, String>  records2_partition1 = consumer.poll(Duration.ofMillis(200));
    assertEquals(1, records2_partition1.count());
    assertEquals("3", records2_partition1.records(partition1).get(0).value());

    ConsumerRecords<String, String>  records2_empty = consumer.poll(Duration.ofMillis(200));
    assertEquals(0, records2_empty.count());

    key++;
    PRODUCER.sendRecord(topicName, 0, String.valueOf(key), String.valueOf(key)).get();
    key++;
    PRODUCER.sendRecord(topicName, 1, String.valueOf(key), String.valueOf(key)).get();

    consumer.pause(Arrays.asList(partition0));
    ConsumerRecords<String, String>  records3_partition0 = consumer.poll(Duration.ofMillis(200));
    assertEquals(0, records3_partition0.count());

    ConsumerRecords<String, String>  records3_partition1 = consumer.poll(Duration.ofMillis(200));
    assertEquals(1, records3_partition1.count());
    assertEquals("5", records3_partition1.records(partition1).get(0).value());

    key++;
    PRODUCER.sendRecord(topicName, 0, String.valueOf(key), String.valueOf(key)).get();
    key++;
    PRODUCER.sendRecord(topicName, 1, String.valueOf(key), String.valueOf(key)).get();

    consumer.resume(Arrays.asList(partition0));

    ConsumerRecords<String, String>  records4_partition0 = consumer.poll(Duration.ofMillis(200));
    assertEquals(2, records4_partition0.count());
    assertEquals("5", records4_partition0.records(partition1).get(0).value());
    assertEquals("7", records4_partition0.records(partition1).get(0).value());

    ConsumerRecords<String, String>  records4_partition1 = consumer.poll(Duration.ofMillis(200));
    assertEquals(1, records4_partition1.count());
    assertEquals("6", records4_partition1.records(partition1).get(0).value());

  }

  /**
   * Creates a topic with only one partition and replication factor 1.
   * @param topicName the name of the topic
   */
  private void createDefaultTopic(String topicName) throws ExecutionException, InterruptedException {
    createDefaultTopic(topicName, 1);
  }

  /**
   *
   * @param topicName the name of the topic
   * @param partitions the number of partitions for the topic
   */
  private void createDefaultTopic(String topicName, int partitions) throws ExecutionException, InterruptedException {
    ADMIN.createTopic(topicName, partitions, REPLICATION_FACTOR);
    topicPool.add(topicName);
  }

  private KafkaConsumer<String, String> createAutoConsumer(String groupId) {
    Properties props = new Properties();
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
    return createConsumer(groupId, props);
  }
  private KafkaConsumer<String, String> createManualConsumer() {
    return createManualConsumer(null);
  }
  private KafkaConsumer<String, String> createManualConsumer(String groupId) {
    return createConsumer(groupId, new Properties());
  }
  private KafkaConsumer<String, String> createConsumer(String groupId, Properties props) {
    props.putAll((Properties) DEFAULT_PROPERTIES.clone());
    if (groupId != null) {
      props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    }
    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
    // Adding all the created consumers to a pool so that no consumer ends up not being closed.
    consumerPool.add(consumer);
    return consumer;
  }
}
