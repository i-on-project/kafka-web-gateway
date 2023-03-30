package com.isel.ps.kafka_clients_demos;

import com.isel.ps.kafka_clients.Consumer;

import java.util.Scanner;

public class ConsumerDemo {

    //private static final Logger log = LogManager.getLogger(ConsumerDemo.class);
    private static final Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) {
        System.out.println("Started consumer\n");
        System.out.print("Input the group id:");
        String groupId = scanner.nextLine();
        Consumer consumer = new Consumer(groupId);
        System.out.println("Input topic name:");
        String topicName = "topic-nr-1";//scanner.nextLine();
        consumer.subscribeTopic(topicName);
        System.out.println(String.format("Subscribed to topic [%s]\n", topicName));
        System.out.println("Starting to consume\n");
         consumer.scuffedConsume();
    }
}
