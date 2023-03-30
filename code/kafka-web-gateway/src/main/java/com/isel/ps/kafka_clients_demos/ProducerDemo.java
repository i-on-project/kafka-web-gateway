package com.isel.ps.kafka_clients_demos;

import com.isel.ps.kafka_clients.Producer;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.sql.Time;
import java.time.Instant;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class ProducerDemo {

    //private static final Logger log = LogManager.getLogger(ProducerDemo.class);
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("Started producer\n");
        Producer producer = new Producer();
        System.out.println("Input the topic name:\n");
        String topicName = "topic-nr-1";//scanner.nextLine();
        System.out.println("Starting to produce\n");

        //int n = 0;
        while(true) {
            //System.out.println(producer.sendRecord(topicName, "" + n++).toString());
            //System.out.println(String.format("Sent: [%d]", n));
            System.out.print(">");
            String message = scanner.nextLine();
            RecordMetadata result = producer.sendRecord(topicName, message);
            System.out.println(String.format("Message sent [%s] \nTo topic [%s] \nTo partition [%d] \nTo offset [%s] \nAt [%s]",
                            message, result.topic(), result.partition(), String.valueOf(result.offset()),
                            Time.from(Instant.ofEpochSecond(result.timestamp())).toString()));


        }



    }
}
