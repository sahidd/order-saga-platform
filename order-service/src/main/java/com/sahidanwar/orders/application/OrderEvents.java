package com.sahidanwar.orders.application;

import com.sahidanwar.platform.SagaJson;
import com.sahidanwar.platform.SagaTypes;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEvents {

    private final OrderSaga saga;

    public OrderEvents(OrderSaga saga) {
        this.saga = saga;
    }

    @KafkaListener(topics = SagaTypes.INVENTORY_TOPIC, groupId = "order-service")
    public void inventory(String payload) {
        saga.onInventory(SagaJson.read(payload));
    }

    @KafkaListener(topics = SagaTypes.PAYMENT_TOPIC, groupId = "order-service")
    public void payment(String payload) {
        saga.onPayment(SagaJson.read(payload));
    }
}
