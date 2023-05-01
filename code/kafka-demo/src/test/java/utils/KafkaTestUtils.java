package utils;

import com.isel.ps.admin.KafkaAdminUtil;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static com.isel.ps.KafkaClientsHelper.BOOTSTRAP_SERVERS;

public class KafkaTestUtils {

    private final KafkaAdminUtil admin = new KafkaAdminUtil(BOOTSTRAP_SERVERS);
    private static final short REPLICATION_FACTOR = 1;

    private final ArrayList<KafkaConsumer> consumerPool = new ArrayList<>();
    private final ArrayList<KafkaProducer> producerPool = new ArrayList<>();
    private final ArrayList<String> topicPool = new ArrayList<>();

    public void reset() {
        consumerPool.forEach(KafkaConsumer::close);
        producerPool.forEach(KafkaProducer::close);
        admin.deleteTopics(topicPool);
    }

    public void stop() {
        reset();
        admin.closeAdminClient();
    }

    /**
     *
     * @param topicName the name of the topic
     * @param partitions the number of partitions for the topic
     */
    public void createDefaultTopic(String topicName, int partitions) throws ExecutionException, InterruptedException {
        admin.createTopic(topicName, partitions, REPLICATION_FACTOR);
        topicPool.add(topicName);
    }

    public KafkaConsumer<String, String> createConsumer(String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        if (groupId != null) {
            props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        }
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        // Adding all the created consumers to a pool so that no consumer ends up not being closed.
        consumerPool.add(consumer);
        return consumer;
    }

    public KafkaProducer<String, String> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        // Adding all the created producers to a pool so that no producer ends up not being closed.
        producerPool.add(producer);
        return producer;

    }
}
