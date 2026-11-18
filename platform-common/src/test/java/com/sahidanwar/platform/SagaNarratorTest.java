package com.sahidanwar.platform;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SagaNarratorTest {

    @Test
    void namesTheFailedStepAndTheCompensation() {
        String text = SagaNarrator.explain("CANCELLED", List.of(
                SagaTypes.ORDER_CREATED,
                SagaTypes.INVENTORY_RESERVED,
                SagaTypes.PAYMENT_FAILED,
                SagaTypes.ORDER_CANCELLED,
                SagaTypes.INVENTORY_RELEASED));
        assertThat(text).contains("payment step failed");
        assertThat(text).contains("released");
        assertThat(text).contains("PaymentFailed");
    }

    @Test
    void explainsAStockRejection() {
        String text = SagaNarrator.explain("CANCELLED", List.of(
                SagaTypes.ORDER_CREATED, SagaTypes.INVENTORY_REJECTED));
        assertThat(text).contains("could not reserve");
        assertThat(text).contains("before payment");
    }

    @Test
    void describesACompletedOrder() {
        String text = SagaNarrator.explain("CONFIRMED", List.of(
                SagaTypes.ORDER_CREATED, SagaTypes.INVENTORY_RESERVED, SagaTypes.PAYMENT_PROCESSED));
        assertThat(text).contains("completed");
        assertThat(text).doesNotContain("failed");
    }
}
