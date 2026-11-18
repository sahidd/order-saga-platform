package com.sahidanwar.orders.application;

import com.sahidanwar.platform.SagaEvent;
import com.sahidanwar.platform.SagaJson;
import com.sahidanwar.platform.SagaTypes;
import com.sahidanwar.orders.domain.CustomerOrder;
import com.sahidanwar.orders.domain.OutboxEvent;
import com.sahidanwar.orders.domain.SagaRecord;
import com.sahidanwar.orders.infrastructure.OrderRepository;
import com.sahidanwar.orders.infrastructure.OutboxRepository;
import com.sahidanwar.orders.infrastructure.SagaRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class OrderCommandService {

    private final OrderRepository orders;
    private final SagaRecordRepository history;
    private final OutboxRepository outbox;

    public OrderCommandService(OrderRepository orders, SagaRecordRepository history, OutboxRepository outbox) {
        this.orders = orders;
        this.history = history;
        this.outbox = outbox;
    }

    @Transactional
    public CustomerOrder place(String customer, String sku, int quantity, String paymentMethod) {
        CustomerOrder order = orders.save(CustomerOrder.pending(customer, sku, quantity, paymentMethod));
        SagaEvent event = new SagaEvent(
                UUID.randomUUID(),
                SagaTypes.ORDER_CREATED,
                order.getId(),
                sku,
                quantity,
                BigDecimal.ZERO.setScale(2),
                paymentMethod,
                null);
        String payload = SagaJson.write(event);
        history.save(SagaRecord.of(event.eventId(), order.getId(), event.type(), payload));
        outbox.save(OutboxEvent.unpublished(SagaTypes.ORDER_TOPIC, order.getId().toString(), payload));
        return order;
    }
}
