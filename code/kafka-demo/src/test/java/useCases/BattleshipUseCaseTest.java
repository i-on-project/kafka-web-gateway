package useCases;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.*;
import utils.KafkaTestUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BattleshipUseCaseTest {
    ExecutorService executor = Executors.newFixedThreadPool(15);
    KafkaTestUtils kafkaTestUtils;

    @BeforeAll
    void setUp() {
        kafkaTestUtils = new KafkaTestUtils();
    }

    @AfterAll
    void tearDown() {
        kafkaTestUtils.reset();
    }

    @AfterEach
    void tearEachDown() {
        kafkaTestUtils.stop();
    }

    /**
     * Rudimentary simulation of a battleship match, with two player submitting their plays to a topic's partition
     * and reading the plays that were validated from another partition from the same topic.
     * Meanwhile, there's a 'validator' reading from the partition the players are submitting to, and writing the
     * valid plays to the partition the players are reading.
     * <p>
     * In addition to that, there's a 'watcher', responsible only for viewing the whole process and printing it
     * to the console.
     */
    @Test
    void makePlaysTest() throws InterruptedException, ExecutionException {
        System.out.println("Starting makePlaysTest test.");
        CountDownLatch latch = new CountDownLatch(4);
        String playsTopicName = "plays-topic";

        // Partition 1 will be plays suggested by players.
        // Partition 2 will be validated plays.
        kafkaTestUtils.createDefaultTopic(playsTopicName, 2);

        Thread.sleep(2000);
        TopicPartition plays = new TopicPartition(playsTopicName, 0);
        TopicPartition validPlays = new TopicPartition(playsTopicName, 1);

        executor.submit(() -> playsWatcher(plays, validPlays, latch));
        executor.submit(() -> playsPlayer(plays, validPlays, "player1", true, latch));
        executor.submit(() -> playsPlayer(plays, validPlays, "player2", false, latch));
        executor.submit(() -> playsValidator(plays, validPlays, latch));

        latch.await();
        System.out.println("--------------- Finished test. ---------------");
    }

    /**
     * Responsible for watching the plays and validPlays partitions and printing the information to the console.
     * @param plays       topic's partition that represents unchecked/non-validated plays.
     * @param validPlays  topic's partition that represents validated plays.
     * @param latch       synchronizer responsible for triggering the end of the test only when all workers end.
     */
    private void playsWatcher(TopicPartition plays, TopicPartition validPlays, CountDownLatch latch) {
        KafkaConsumer<String, String> consumer = kafkaTestUtils.createConsumer(null);

        consumer.assign(List.of(plays, validPlays));
        consumer.seekToBeginning(List.of(plays, validPlays));

        AtomicBoolean exit = new AtomicBoolean(false);
        while (!exit.get()) {
            consumer.poll(Duration.ofMinutes(30)).forEach(record -> {
                exit.set(record.value() != null && record.value().equals("exit"));
                String partition = "invalid..";
                if (record.partition() == 0) partition = "plays-partition";
                if (record.partition() == 1) partition = "valid-plays-partition";
                System.out.println(String.format("partition: [%s]\nkey: [%s]    value: [%s]\noffset: [%d]\n", partition, record.key(), record.value(), record.offset()));
                System.out.println("************************************");
            });
        }
        System.out.println("--------------- Closing watcher. --------------------");
        latch.countDown();
    }

    /**
     * @param plays       topic's partition that represents unchecked/non-validated plays.
     * @param validPlays  topic's partition that represents validated plays.
     * @param id          string that identifies the player.
     * @param firstToPlay is this player first to play
     * @param latch       synchronizer responsible for triggering the end of the test only when all workers end.
     */
    private void playsPlayer(TopicPartition plays, TopicPartition validPlays, String id, boolean firstToPlay, CountDownLatch latch) {
        KafkaConsumer<String, String> consumer = kafkaTestUtils.createConsumer(null);
        KafkaProducer<String, String> producer = kafkaTestUtils.createProducer();

        consumer.assign(Collections.singleton(validPlays));
        consumer.seekToBeginning(Collections.singleton(validPlays));

        if (firstToPlay) {
            producer.send(new ProducerRecord<>(
                    plays.topic(),
                    plays.partition(),
                    id,
                    id
            ));
        } else {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                producer.send(new ProducerRecord<>(
                        validPlays.topic(),
                        validPlays.partition(),
                        id,
                        "exit"
                ));
                return;
            }
        }

        AtomicBoolean exit = new AtomicBoolean(false);
        for (AtomicInteger i = new AtomicInteger(); i.get() < 10 && !exit.get();) {
            consumer.poll(Duration.ofMinutes(10)).records(validPlays).forEach(record -> {

                exit.set(record.value() != null && record.value().equals("exit"));
                if (record.value() != null && !record.value().equals(id)) {
                    i.incrementAndGet();
                    producer.send(new ProducerRecord<>(
                            plays.topic(),
                            plays.partition(),
                            id,
                            id
                    ));
                }

            });
        }
        producer.send(new ProducerRecord<>(
                plays.topic(),
                plays.partition(),
                id,
                "exit"
        ));
        System.out.println("--------------- Closing " + id + ". ---------------");
        latch.countDown();
    }

    /**
     *
     * @param plays       topic's partition that represents unchecked/non-validated plays.
     * @param validPlays  topic's partition that represents validated plays.
     * @param latch       synchronizer responsible for triggering the end of the test only when all workers end.
     */
    private void playsValidator(TopicPartition plays, TopicPartition validPlays, CountDownLatch latch) {
        KafkaConsumer<String, String> validatorConsumer = kafkaTestUtils.createConsumer(null);
        KafkaProducer<String, String> validatorProducer = kafkaTestUtils.createProducer();

        validatorConsumer.assign(Collections.singleton(plays));
        validatorConsumer.seekToBeginning(Collections.singleton(plays));

        AtomicBoolean exit = new AtomicBoolean(false);
        while (!exit.get()) {
            validatorConsumer.poll(Duration.ofSeconds(5)).records(plays).forEach(record -> {

                exit.set(record.value() != null && record.value().equals("exit"));
                validatorProducer.send(
                        new ProducerRecord<>(
                                validPlays.topic(),
                                validPlays.partition(),
                                "validator",
                                record.value()
                        )
                );
            });
        }
        System.out.println("--------------- Closing validator. ---------------");
        latch.countDown();
    }

    @Test
    void findMatchTest() throws InterruptedException, ExecutionException {
        System.out.println("Starting findMatchTest test.");

        int numberOfPlayers = 10;

        CyclicBarrier cyclicBarrier = new CyclicBarrier(numberOfPlayers);
        CountDownLatch finalLatch = new CountDownLatch(numberOfPlayers + 1);

        String topicName = "matchmaking-topic";
        kafkaTestUtils.createDefaultTopic(topicName, 2);
        TopicPartition seekersPool = new TopicPartition(topicName, 0);
        TopicPartition createdMatches = new TopicPartition(topicName, 1);

        executor.submit(() -> matchMaker(seekersPool, createdMatches, finalLatch));
        Thread.sleep(1000);
        for (int i = 0; i < numberOfPlayers;) {
            int id = i;
            ++i;
            executor.submit(() -> matchSeekerPlayer(seekersPool, createdMatches, finalLatch, cyclicBarrier, id));
        }


        finalLatch.await();
        System.out.println("Finished findMatchTest");
    }

    private void matchSeekerPlayer(TopicPartition seekersPool, TopicPartition createdMatches, CountDownLatch finalLatch, CyclicBarrier cyclicBarrier, int id) {
        try {
            cyclicBarrier.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        KafkaConsumer<String, String> consumer = kafkaTestUtils.createConsumer(null);
        KafkaProducer<String, String> producer = kafkaTestUtils.createProducer();
        String playerId = "player " + id;

        producer.send(new ProducerRecord<>(seekersPool.topic(), seekersPool.partition(), playerId, playerId));

        consumer.assign(Collections.singleton(createdMatches));
        consumer.seekToBeginning(Collections.singleton(createdMatches));
        AtomicBoolean exit = new AtomicBoolean(false);
        while (!exit.get()) {
            consumer.poll(Duration.ofMinutes(2)).records(createdMatches).forEach(record -> {
                if (record.key().equals(playerId) || record.value().equals(playerId)) {
                    exit.set(false);
                    System.out.println(String.format("[%s] matched with [%s]", record.key(), record.value()));
                }
            });
        }

        try {
            cyclicBarrier.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        producer.send(new ProducerRecord<>(seekersPool.topic(), seekersPool.partition(), "exit", "exit"));

        finalLatch.countDown();
    }

    private void matchMaker(TopicPartition seekersPool, TopicPartition createdMatches, CountDownLatch finalLatch) {
        Phaser phaser = new Phaser();
        phaser.register();

        executor.submit(() -> matchMakerConsumer(phaser, seekersPool, createdMatches));

        phaser.arriveAndAwaitAdvance();
        finalLatch.countDown();
    }

    private void matchMakerConsumer(Phaser phaser, TopicPartition seekersPool, TopicPartition createdMatches) {
        phaser.register();
        // Holds all the players requesting to join a match.
        // Unfortunately allows duplicates but that's not the focus here.
        BlockingQueue<String> currentSeekers = new LinkedBlockingQueue<>();

        KafkaConsumer<String, String> consumer = kafkaTestUtils.createConsumer(null);
        consumer.assign(Collections.singleton(seekersPool));
        consumer.seekToBeginning(Collections.singleton(seekersPool));

        AtomicBoolean exit = new AtomicBoolean(false);
        while (!exit.get()) {
            consumer.poll(Duration.ofSeconds(5)).records(seekersPool).forEach(record -> {
                if (record.value() != null && record.value().equals("exit")) {
                    exit.set(true);
                } else {
                    currentSeekers.add(record.value());
                }
            });
            phaser.register();
            executor.submit(() -> matchMakerProducer(phaser, currentSeekers, createdMatches));
        }
        phaser.arrive();
    }

    private void matchMakerProducer(Phaser phaser, BlockingQueue<String> currentSeekers, TopicPartition createdMatches) {
        KafkaProducer<String, String> producer = kafkaTestUtils.createProducer();

        String playerA = null;
        String playerB = null;
        while (currentSeekers.size() > 1) {
            // extract a player from the queue, if there's none, wait, if time expires, return null.
            try {
                playerA = currentSeekers.poll(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                playerA = null;
            }
            try {
                playerB = currentSeekers.poll(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                playerB = null;
            }

            if (playerA == null || playerB == null) {
                if (playerA != null) currentSeekers.add(playerA);
                if (playerB != null) currentSeekers.add(playerB);
                continue;
            }
            // Probably should send 2 records, one with a key and a value of each, but here it isn't the focus.
            producer.send(new ProducerRecord<>(createdMatches.topic(), createdMatches.partition(), playerA, playerB));
        }

        phaser.arrive();
    }
}
