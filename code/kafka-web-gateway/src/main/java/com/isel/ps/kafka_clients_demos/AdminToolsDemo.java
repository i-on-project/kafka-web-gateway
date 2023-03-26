package com.isel.ps.kafka_clients_demos;

import com.isel.ps.kafka_clients.AdminTools;

import java.util.concurrent.ExecutionException;

public class AdminToolsDemo {
    //private static final Logger log = LogManager.getLogger(ProducerDemo.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        AdminTools admin = new AdminTools();
        admin.createTopic("demo-topic", 5, (short) 3);
        System.out.println("Existing topics names:");
        for (String topic : admin.getAllTopics().get()) {
            System.out.println(String.format("topic: [%s]", topic));
        }

    }
}
