package com.sahidanwar.platform;

import java.util.List;

public final class SagaNarrator {

    private SagaNarrator() {
    }

    public static String explain(String status, List<String> eventTypes) {
        List<String> types = eventTypes == null ? List.of() : eventTypes;
        String events = types.isEmpty() ? "none recorded yet" : String.join(", ", types);
        String outcome;
        if (types.contains(SagaTypes.PAYMENT_FAILED)) {
            outcome = types.contains(SagaTypes.INVENTORY_RELEASED)
                    ? "The payment step failed, and the reserved inventory was released."
                    : "The payment step failed. Compensation to release inventory has been requested.";
        } else if (types.contains(SagaTypes.INVENTORY_REJECTED)) {
            outcome = "Inventory could not reserve the stock, so the order was cancelled before payment.";
        } else if ("CONFIRMED".equals(status) || types.contains(SagaTypes.PAYMENT_PROCESSED)) {
            outcome = "The order completed. Inventory was reserved and payment was processed.";
        } else {
            outcome = "The order is " + status + ".";
        }
        return outcome + " Events: " + events + ".";
    }
}
