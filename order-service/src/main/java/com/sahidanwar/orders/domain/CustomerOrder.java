package com.sahidanwar.orders.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class CustomerOrder {

    @Id
    private UUID id;

    @Column(name = "customer_name", nullable = false, length = 80)
    private String customerName;

    @Column(nullable = false, length = 40)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", nullable = false, length = 40)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected CustomerOrder() {
    }

    public static CustomerOrder pending(String customer, String sku, int quantity, String paymentMethod) {
        CustomerOrder order = new CustomerOrder();
        order.id = UUID.randomUUID();
        order.customerName = customer;
        order.sku = sku;
        order.quantity = quantity;
        order.amount = BigDecimal.ZERO.setScale(2);
        order.paymentMethod = paymentMethod;
        order.status = OrderStatus.PENDING;
        order.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        return order;
    }

    public void reserve(BigDecimal price) {
        this.amount = price;
        this.status = OrderStatus.RESERVED;
    }

    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
    }

    public void compensate() {
        this.status = OrderStatus.COMPENSATING;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
