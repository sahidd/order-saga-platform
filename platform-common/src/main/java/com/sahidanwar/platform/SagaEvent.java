package com.sahidanwar.platform;

import java.math.BigDecimal;
import java.util.UUID;

public record SagaEvent(
        UUID eventId,
        String type,
        UUID orderId,
        String sku,
        int quantity,
        BigDecimal amount,
        String paymentMethod,
        String reason
) {
}
