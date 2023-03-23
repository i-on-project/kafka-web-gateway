package com.isel.ps.kafka_clients;


import com.isel.ps.Constants;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Date;
import java.time.Instant;
import java.util.Properties;

public class Producer {
    private static Logger log = LogManager.getLogger(Producer.class);

    public Producer() {
    }

    public static void main(String[] args) {
        String bootstrapServers = System.getenv(Constants.KAFKA_URL);

        // Creating Producer properties (configurations)
        Properties producerProperties = new Properties();
        producerProperties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProperties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProperties);

        producer.send(new ProducerRecord<>("demo-topic", "[" + Date.from(Instant.ofEpochSecond(System.currentTimeMillis() / 1000)) + "]"));
        producer.close();
    }

}
