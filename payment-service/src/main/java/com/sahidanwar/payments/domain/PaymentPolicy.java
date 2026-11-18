package com.sahidanwar.payments.domain;

public final class PaymentPolicy {

    private PaymentPolicy() {
    }

    public static boolean approved(String paymentMethod) {
        if (paymentMethod == null) {
            return false;
        }
        return !"declined".equalsIgnoreCase(paymentMethod.trim());
    }
}
