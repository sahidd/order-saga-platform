package com.sahidanwar.payments;

import com.sahidanwar.payments.domain.PaymentPolicy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentPolicyTest {

    @Test
    void declinesOnlyTheDeclinedMethod() {
        assertThat(PaymentPolicy.approved("card")).isTrue();
        assertThat(PaymentPolicy.approved("wallet")).isTrue();
        assertThat(PaymentPolicy.approved("declined")).isFalse();
        assertThat(PaymentPolicy.approved(" Declined ")).isFalse();
        assertThat(PaymentPolicy.approved(null)).isFalse();
    }
}
