package com.isel.ps.kafka_clients_demos;

import com.isel.ps.kafka_clients.Consumer;
import com.isel.ps.kafka_clients.Producer;

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

        int n = 0;
        while(true) {
            System.out.println(producer.sendRecord(topicName, "" + n++).toString());
            System.out.println(String.format("Sent: [%d]", n));
        }



    }
}
