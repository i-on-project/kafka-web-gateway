package com.isel.ps.kafka_clients;


import com.isel.ps.Constants;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class Producer {
    //private static final Logger log = LogManager.getLogger(Producer.class);

    private final KafkaProducer<String, String> producer;
    private static final String SERVER = System.getenv(Constants.KAFKA_URL);

    public Producer() {
        // Creating Producer properties (configurations)
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.KAFKA_URL);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        //props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "shared-transaction-id");
        this.producer = new KafkaProducer<>(props);
        //producer.initTransactions();
    }


    public RecordMetadata sendRecord(String topicName, String record) throws ExecutionException, InterruptedException {

        //try {
            //producer.beginTransaction();
             // try: "[" + Date.from(Instant.ofEpochSecond(System.currentTimeMillis() / 1000)) + "]"
            //producer.commitTransaction();
        //} catch (ProducerFencedException | OutOfOrderSequenceException | AuthorizationException e) {
            //producer.close();
            //log.error(e.getStackTrace());
            //return false;
        //} catch (KafkaException e) {
            //producer.abortTransaction();
            //log.error(e.getStackTrace());
        //}
        return producer.send(new ProducerRecord<>(topicName, record, record)).get();
    }

    public static void main(String[] args) {

    }

}
