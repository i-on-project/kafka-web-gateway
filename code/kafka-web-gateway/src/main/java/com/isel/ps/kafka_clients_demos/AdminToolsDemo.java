package com.isel.ps.kafka_clients_demos;

import com.isel.ps.kafka_clients.AdminTools;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class AdminToolsDemo {
    //private static final Logger log = LogManager.getLogger(ProducerDemo.class);
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        AdminTools admin = new AdminTools();
        System.out.println("Started Administration Tools");
        System.out.print("\nInput the topic name you want to create:");
        String topicname = scanner.nextLine();
        System.out.print("\nInput the number of partitions for the topic:");
        int partitions = scanner.nextInt();
        System.out.print("\nInput the replication factor:");
        short replicationFactor = scanner.nextShort();
        admin.createTopic(topicname, partitions, replicationFactor);
        System.out.println("Wait a bit and click enter to get all topics..");
        scanner.next();
        System.out.println("Existing topics names:");
        for (String topic : admin.getAllTopics().get()) {
            System.out.println(String.format("topic: [%s]", topic));
        }

    }
}
