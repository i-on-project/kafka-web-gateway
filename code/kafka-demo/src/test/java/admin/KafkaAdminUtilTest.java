package admin;

import static org.junit.jupiter.api.Assertions.*;

import com.isel.ps.admin.KafkaAdminUtil;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.errors.InvalidReplicationFactorException;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.junit.jupiter.api.*;

public class KafkaAdminUtilTest {
  private static final String BOOTSTRAP_SERVERS = "localhost:29092";
  private static final int TEST_NUM_PARTITIONS = 3;
  private static final short TEST_REPLICATION_FACTOR = 3;

  private static KafkaAdminUtil kafkaAdminUtil;

  @BeforeAll
  public static void setUp() {
    kafkaAdminUtil = new KafkaAdminUtil(BOOTSTRAP_SERVERS);
  }

  @AfterAll
  public static void tearDown() {
    kafkaAdminUtil.closeAdminClient();
  }

  @Test
  public void testCreateTopic() throws InterruptedException, ExecutionException {
    final String topic = generateTopic();
    kafkaAdminUtil.createTopic(topic, TEST_NUM_PARTITIONS, TEST_REPLICATION_FACTOR);
    while (!kafkaAdminUtil.hasTopic(topic)) {
      Thread.onSpinWait();
    }
    TopicDescription topicDescription = kafkaAdminUtil.describeTopic(topic);
    assertEquals(topicDescription.name(), topic);
    kafkaAdminUtil.deleteTopic(topic);
  }

  @Test
  public void testCreateTopicWithInvalidReplicationFactor() {
    final String topic = generateTopic();
    try {
      kafkaAdminUtil.createTopic(topic, TEST_NUM_PARTITIONS, (short) (TEST_REPLICATION_FACTOR + 1));
    } catch (Exception e) {
      assertInstanceOf(InvalidReplicationFactorException.class, e.getCause());
    }
  }

  @Test
  public void testCreateExistingTopic() throws InterruptedException, ExecutionException {
    final String topic = generateTopic();
    try {
      kafkaAdminUtil.createTopic(topic, TEST_NUM_PARTITIONS, TEST_REPLICATION_FACTOR);
      kafkaAdminUtil.createTopic(topic, TEST_NUM_PARTITIONS, TEST_REPLICATION_FACTOR);
    } catch (Exception e) {
      assertInstanceOf(TopicExistsException.class, e.getCause());
    } finally {
      kafkaAdminUtil.deleteTopic(topic);
    }
  }

  @Test
  public void testDeleteNonExistingTopic() {
    final String topic = generateTopic();
    try {
      kafkaAdminUtil.deleteTopic(topic);
    } catch (Exception e) {
      assertInstanceOf(UnknownTopicOrPartitionException.class, e.getCause());
    }
  }

  @Test
  public void testDeleteTopic() throws InterruptedException, ExecutionException {
    final String topic = generateTopic();
    kafkaAdminUtil.createTopic(topic, TEST_NUM_PARTITIONS, TEST_REPLICATION_FACTOR);
    DeleteTopicsResult deleteTopicsResult =
        kafkaAdminUtil.deleteTopics(Collections.singletonList(topic));
    assertNotNull(deleteTopicsResult);
  }

  @Test
  public void testDescribeExistingTopic() throws InterruptedException, ExecutionException {
    final String topic = generateTopic();
    kafkaAdminUtil.createTopic(topic, TEST_NUM_PARTITIONS, TEST_REPLICATION_FACTOR);
    while (!kafkaAdminUtil.hasTopic(topic)) {
      Thread.onSpinWait();
    }
    assertNotNull(kafkaAdminUtil.describeTopic(topic));
    kafkaAdminUtil.deleteTopic(topic);
  }

  @Test
  public void testDescribeNonExistingTopic() {
    final String topic = generateTopic();
    try {
      kafkaAdminUtil.describeTopic(topic);
    } catch (Exception e) {
      assertInstanceOf(UnknownTopicOrPartitionException.class, e.getCause());
    }
  }

  private static String generateTopic() {
    return UUID.randomUUID().toString();
  }
}
