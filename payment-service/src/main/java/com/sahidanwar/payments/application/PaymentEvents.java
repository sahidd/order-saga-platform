package com.sahidanwar.payments.application;

import com.sahidanwar.platform.SagaEvent;
import com.sahidanwar.platform.SagaJson;
import com.sahidanwar.platform.SagaTypes;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEvents {

    private final PaymentService payments;

    public PaymentEvents(PaymentService payments) {
        this.payments = payments;
    }

    @KafkaListener(topics = SagaTypes.INVENTORY_TOPIC, groupId = "payment-service")
    public void onInventory(String payload) {
        SagaEvent event = SagaJson.read(payload);
        payments.onInventoryReserved(event);
    }
}
