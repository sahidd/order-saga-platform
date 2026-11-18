package com.sahidanwar.payments.application;

import com.sahidanwar.payments.domain.OutboxEvent;
import com.sahidanwar.payments.domain.Payment;
import com.sahidanwar.payments.domain.PaymentPolicy;
import com.sahidanwar.payments.domain.ProcessedEvent;
import com.sahidanwar.payments.infrastructure.OutboxRepository;
import com.sahidanwar.payments.infrastructure.PaymentRepository;
import com.sahidanwar.payments.infrastructure.ProcessedEventRepository;
import com.sahidanwar.platform.SagaEvent;
import com.sahidanwar.platform.SagaJson;
import com.sahidanwar.platform.SagaTypes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {

    static final String DECLINED_REASON =
            "Payment was declined. No charge was captured, so compensation releases inventory.";

    private final PaymentRepository payments;
    private final OutboxRepository outbox;
    private final ProcessedEventRepository processed;

    public PaymentService(PaymentRepository payments, OutboxRepository outbox, ProcessedEventRepository processed) {
        this.payments = payments;
        this.outbox = outbox;
        this.processed = processed;
    }

    @Transactional(readOnly = true)
    public Payment get(UUID orderId) {
        return payments.findById(orderId).orElse(null);
    }

    @Transactional
    public void onInventoryReserved(SagaEvent event) {
        if (!SagaTypes.INVENTORY_RESERVED.equals(event.type()) || !accept(event.eventId())) {
            return;
        }
        if (payments.existsById(event.orderId())) {
            return;
        }
        boolean approved = PaymentPolicy.approved(event.paymentMethod());
        String reason = approved ? null : DECLINED_REASON;
        BigDecimal amount = event.amount() == null ? BigDecimal.ZERO.setScale(2) : event.amount();
        payments.save(Payment.record(
                event.orderId(),
                approved ? "PROCESSED" : "FAILED",
                amount,
                event.paymentMethod(),
                reason));
        SagaEvent outgoing = new SagaEvent(
                UUID.randomUUID(),
                approved ? SagaTypes.PAYMENT_PROCESSED : SagaTypes.PAYMENT_FAILED,
                event.orderId(),
                event.sku(),
                event.quantity(),
                amount,
                event.paymentMethod(),
                reason);
        outbox.save(OutboxEvent.unpublished(
                SagaTypes.PAYMENT_TOPIC, event.orderId().toString(), SagaJson.write(outgoing)));
    }

    private boolean accept(UUID eventId) {
        if (processed.existsById(eventId)) {
            return false;
        }
        processed.save(ProcessedEvent.of(eventId));
        return true;
    }
}
