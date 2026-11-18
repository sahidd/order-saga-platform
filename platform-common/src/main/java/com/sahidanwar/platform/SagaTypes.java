package com.sahidanwar.platform;

public final class SagaTypes {

    public static final String ORDER_CREATED = "OrderCreated";
    public static final String ORDER_CANCELLED = "OrderCancelled";
    public static final String INVENTORY_RESERVED = "InventoryReserved";
    public static final String INVENTORY_REJECTED = "InventoryRejected";
    public static final String INVENTORY_RELEASED = "InventoryReleased";
    public static final String PAYMENT_PROCESSED = "PaymentProcessed";
    public static final String PAYMENT_FAILED = "PaymentFailed";

    public static final String ORDER_TOPIC = "order.events";
    public static final String INVENTORY_TOPIC = "inventory.events";
    public static final String PAYMENT_TOPIC = "payment.events";

    private SagaTypes() {
    }
}
