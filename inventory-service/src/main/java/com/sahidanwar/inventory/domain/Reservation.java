package com.sahidanwar.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @Column(name = "order_id")
    private UUID orderId;

    @Column(nullable = false, length = 40)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private boolean released;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected Reservation() {
    }

    public static Reservation hold(UUID orderId, String sku, int quantity) {
        Reservation reservation = new Reservation();
        reservation.orderId = orderId;
        reservation.sku = sku;
        reservation.quantity = quantity;
        reservation.released = false;
        reservation.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        return reservation;
    }

    public void release() {
        this.released = true;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isReleased() {
        return released;
    }
}
