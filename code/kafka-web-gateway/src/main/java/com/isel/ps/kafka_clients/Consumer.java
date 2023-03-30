package com.isel.ps.kafka_clients;

import com.isel.ps.Constants;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.sql.Time;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

public class Consumer {

    //private static final Logger log = LogManager.getLogger(Consumer.class);

    private final KafkaConsumer<String, String> consumer;

    public Consumer(String groupId) {
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.KAFKA_URL);
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty("enable.auto.commit", "true");
        props.setProperty("auto.commit.interval.ms", "1000");

        this.consumer = new KafkaConsumer<>(props);
    }

    public void subscribeTopic(String topicName) {
        consumer.subscribe(Arrays.asList(topicName));
        //consumer.assign(Arrays.asList(new TopicPartition(topicName, 0), new TopicPartition(topicName, 1)));
    }

    public void scuffedConsume() {
        while (true) {
            try {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println(String.format("Message sent with key: [%s] and value: [%s] \nTo topic [%s] \nTo partition [%d] \nTo offset [%s] \nAt [%s] \nHeaders: [%s] \n LeaderEpoch: [%d]\n\n",
                            record.key(), record.value(), record.topic(), record.partition(), record.offset(), Time.from(Instant.ofEpochSecond(record.timestamp())).toString(), record.headers().toString(), record.leaderEpoch().isPresent() ? record.leaderEpoch().get() : -666));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {

    }

}
