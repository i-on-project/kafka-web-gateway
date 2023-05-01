package consumer;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.KafkaTestUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KafkaConsumerMultiThreadedTest {

    // private static final Logger log = LoggerFactory.getLogger(LockingIncorrectUsageTests.class);
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerMultiThreadedTest.class);

    ExecutorService executor = Executors.newFixedThreadPool(15);
    KafkaTestUtils kafkaUtils;

    @BeforeAll
    void setUp() {
        kafkaUtils = new KafkaTestUtils();
    }

    @AfterAll
    void tearDown() {
        kafkaUtils.reset();
    }

    @AfterEach
    void tearEachDown() {
        kafkaUtils.stop();
    }

    @Test
    void cancelLongPollingTest () throws ExecutionException, InterruptedException {
        logger.info("Starting cancelLongPollingTest.");
        CountDownLatch latch = new CountDownLatch(1);

        kafkaUtils.createDefaultTopic("cancelLongPollingTestPartition", 1);
        KafkaConsumer<String, String> consumer = kafkaUtils.createConsumer(null);
        TopicPartition partition = new TopicPartition("cancelLongPollingTestPartition", 0);
        consumer.assign(Collections.singleton(partition));
        consumer.seekToBeginning(Collections.singleton(partition));

        logger.info("Launched thread responsible for polling.");
        executor.submit(() -> {
            logger.info("Before poll().");
            // Starting blocking, call of poll
            try {
                consumer.poll(Duration.ofMinutes(10));
            } catch (WakeupException ex) {
                logger.info("Caught WakeupException (poll was canceled/woken up).");
            } finally {
                logger.info("After poll() in finally.");
                latch.countDown();
            }
            logger.info("After poll() after finally.");

        });
        Thread.sleep(10000);

        logger.info("Before 'wakeup' to consumer.");
        consumer.wakeup();
        logger.info("After 'wakeup' to consumer.");
        latch.await();

    }

}
