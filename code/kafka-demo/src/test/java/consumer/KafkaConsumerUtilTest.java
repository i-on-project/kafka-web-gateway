package consumer;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/*
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaConsumerUtilTest {
  private static final String TOPIC_NAME = "test-topic";
  private static final int PARTITION_COUNT = 3;
  private static final int RECORD_COUNT = 10;
  private KafkaTestHelper kafkaTestHelper;
  private Properties consumerProps;

  @BeforeAll
  void setUp() {
    kafkaTestHelper = new KafkaTestHelper();
    kafkaTestHelper.setUp();
    consumerProps = kafkaTestHelper.getConsumerProperties("test-group");
  }

  @AfterAll
  void tearDown() {
    kafkaTestHelper.tearDown();
  }

  @Test
  void testConsumeRecords() {
    kafkaTestHelper.produceRecords(TOPIC_NAME, PARTITION_COUNT, RECORD_COUNT);
    KafkaConsumerUtil<String, String> consumerUtil =
        new KafkaConsumerUtil<>(consumerProps, TOPIC_NAME);

    List<ConsumerRecord<String, String>> records = consumerUtil.consumeRecords();
    assertEquals(PARTITION_COUNT * RECORD_COUNT, records.size());

    consumerUtil.close();
  }

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
}
 */
