package com.example.notificationservice.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    @KafkaListener(topics = "orders-topic", groupId = "notification-group")
    public void listenOrders(ConsumerRecord<String, Object> record) {
        System.out.println("[Notification] orders-topic received key=" + record.key() + " value=" + record.value());
        // In a real system: send email/SMS/push notification here
    }

    @KafkaListener(topics = "inventory-topic", groupId = "notification-group")
    public void listenInventory(ConsumerRecord<String, Object> record) {
        System.out.println("[Notification] inventory-topic received key=" + record.key() + " value=" + record.value());
    }
}
