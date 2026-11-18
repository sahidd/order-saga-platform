package com.sahidanwar.orders.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "saga_events")
public class SagaRecord {

    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "event_type", nullable = false, length = 40)
    private String eventType;

    @Column(nullable = false, length = 4000)
    private String payload;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected SagaRecord() {
    }

    public static SagaRecord of(UUID eventId, UUID orderId, String eventType, String payload) {
        SagaRecord record = new SagaRecord();
        record.eventId = eventId;
        record.orderId = orderId;
        record.eventType = eventType;
        record.payload = payload.length() <= 4000 ? payload : payload.substring(0, 4000);
        record.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        return record;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
