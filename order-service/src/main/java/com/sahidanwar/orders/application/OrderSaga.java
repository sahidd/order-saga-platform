package com.sahidanwar.orders.application;

import com.sahidanwar.platform.SagaEvent;
import com.sahidanwar.platform.SagaJson;
import com.sahidanwar.platform.SagaTypes;
import com.sahidanwar.orders.domain.CustomerOrder;
import com.sahidanwar.orders.domain.OrderStatus;
import com.sahidanwar.orders.domain.OutboxEvent;
import com.sahidanwar.orders.domain.ProcessedEvent;
import com.sahidanwar.orders.domain.SagaRecord;
import com.sahidanwar.orders.infrastructure.OrderRepository;
import com.sahidanwar.orders.infrastructure.OutboxRepository;
import com.sahidanwar.orders.infrastructure.ProcessedEventRepository;
import com.sahidanwar.orders.infrastructure.SagaRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderSaga {

    private final OrderRepository orders;
    private final SagaRecordRepository history;
    private final OutboxRepository outbox;
    private final ProcessedEventRepository processed;

    public OrderSaga(OrderRepository orders, SagaRecordRepository history, OutboxRepository outbox,
                     ProcessedEventRepository processed) {
        this.orders = orders;
        this.history = history;
        this.outbox = outbox;
        this.processed = processed;
    }

    @Transactional
    public void onInventory(SagaEvent event) {
        if (!accept(event)) {
            return;
        }
        CustomerOrder order = orders.findById(event.orderId()).orElse(null);
        if (order == null) {
            return;
        }
        if (SagaTypes.INVENTORY_RESERVED.equals(event.type()) && order.getStatus() == OrderStatus.PENDING) {
            order.reserve(event.amount());
            applyPaymentAlreadyRecorded(order);
        } else if (SagaTypes.INVENTORY_REJECTED.equals(event.type()) && order.getStatus() == OrderStatus.PENDING) {
            order.cancel();
        } else if (SagaTypes.INVENTORY_RELEASED.equals(event.type()) && order.getStatus() == OrderStatus.COMPENSATING) {
            order.cancel();
        }
    }

    @Transactional
    public void onPayment(SagaEvent event) {
        if (!accept(event)) {
            return;
        }
        CustomerOrder order = orders.findById(event.orderId()).orElse(null);
        if (order == null || order.getStatus() == OrderStatus.PENDING) {
            return;
        }
        applyPayment(order, event.type(), event.reason());
    }

    private void applyPaymentAlreadyRecorded(CustomerOrder order) {
        List<String> types = history.findByOrderIdOrderByCreatedAtAsc(order.getId()).stream()
                .map(SagaRecord::getEventType)
                .toList();
        if (types.contains(SagaTypes.PAYMENT_FAILED)) {
            applyPayment(order, SagaTypes.PAYMENT_FAILED, "Payment was declined.");
        } else if (types.contains(SagaTypes.PAYMENT_PROCESSED)) {
            applyPayment(order, SagaTypes.PAYMENT_PROCESSED, null);
        }
    }

    private void applyPayment(CustomerOrder order, String type, String reason) {
        if (order.getStatus() != OrderStatus.RESERVED) {
            return;
        }
        if (SagaTypes.PAYMENT_PROCESSED.equals(type)) {
            order.confirm();
            return;
        }
        if (SagaTypes.PAYMENT_FAILED.equals(type)) {
            order.compensate();
            enqueueCancel(order, reason == null ? "Payment was declined." : reason);
        }
    }

    private boolean accept(SagaEvent event) {
        if (processed.existsById(event.eventId())) {
            return false;
        }
        processed.save(ProcessedEvent.of(event.eventId()));
        history.save(SagaRecord.of(event.eventId(), event.orderId(), event.type(), SagaJson.write(event)));
        return true;
    }

    private void enqueueCancel(CustomerOrder order, String reason) {
        SagaEvent cancel = new SagaEvent(
                UUID.randomUUID(),
                SagaTypes.ORDER_CANCELLED,
                order.getId(),
                order.getSku(),
                order.getQuantity(),
                order.getAmount(),
                order.getPaymentMethod(),
                reason);
        String payload = SagaJson.write(cancel);
        history.save(SagaRecord.of(cancel.eventId(), order.getId(), cancel.type(), payload));
        outbox.save(OutboxEvent.unpublished(SagaTypes.ORDER_TOPIC, order.getId().toString(), payload));
    }
}
