package com.sahidanwar.inventory.application;

import com.sahidanwar.platform.SagaEvent;
import com.sahidanwar.platform.SagaJson;
import com.sahidanwar.platform.SagaTypes;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEvents {

    private final InventoryService inventory;

    public InventoryEvents(InventoryService inventory) {
        this.inventory = inventory;
    }

    @KafkaListener(topics = SagaTypes.ORDER_TOPIC, groupId = "inventory-service")
    public void onOrder(String payload) {
        SagaEvent event = SagaJson.read(payload);
        if (SagaTypes.ORDER_CREATED.equals(event.type())) {
            inventory.onOrderCreated(event);
        } else if (SagaTypes.ORDER_CANCELLED.equals(event.type())) {
            inventory.onOrderCancelled(event);
        }
    }
}
