package com.isel.ps.kafka_clients;


import com.isel.ps.Constants;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Date;
import java.time.Instant;
import java.util.Properties;

public class Producer {
    private static final Logger log = LogManager.getLogger(Producer.class);

    private final KafkaProducer<String, String> producer;
    private static final String SERVER = System.getenv(Constants.KAFKA_URL);

    public Producer() {
        // Creating Producer properties (configurations)
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, SERVER);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        this.producer = new KafkaProducer<>(props);
    }


    public boolean sendStringRecord(String topicName, String record) {
        producer.initTransactions();

        try {
            producer.beginTransaction();
            producer.send(new ProducerRecord<>(topicName, record)); // try: "[" + Date.from(Instant.ofEpochSecond(System.currentTimeMillis() / 1000)) + "]"
            producer.commitTransaction();
        } catch (ProducerFencedException e) {
            producer.close();
            log.error(e.getStackTrace());
            return false;
        } catch (KafkaException e) {
            producer.abortTransaction();
            log.error(e.getStackTrace());
        }
        return true;
    }

    public static void main(String[] args) {

    }

}
