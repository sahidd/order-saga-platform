package com.sahidanwar.payments.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @Column(name = "order_id")
    private UUID orderId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 40)
    private String method;

    @Column(length = 200)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected Payment() {
    }

    public static Payment record(UUID orderId, String status, BigDecimal amount, String method, String reason) {
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.status = status;
        payment.amount = amount == null ? BigDecimal.ZERO.setScale(2) : amount;
        payment.method = method;
        payment.reason = reason;
        payment.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        return payment;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getMethod() {
        return method;
    }

    public String getReason() {
        return reason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
