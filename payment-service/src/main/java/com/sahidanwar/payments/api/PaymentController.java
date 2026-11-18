package com.sahidanwar.payments.api;

import com.sahidanwar.payments.domain.DomainException;
import com.sahidanwar.payments.domain.Payment;
import com.sahidanwar.payments.application.PaymentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService payments;

    public PaymentController(PaymentService payments) {
        this.payments = payments;
    }

    @GetMapping("/{orderId}")
    PaymentResponse get(@PathVariable UUID orderId) {
        Payment payment = payments.get(orderId);
        if (payment == null) {
            throw new DomainException("not-found", "Payment was not found.", 404);
        }
        return PaymentResponse.from(payment);
    }

    public record PaymentResponse(
            UUID orderId,
            String status,
            BigDecimal amount,
            String method,
            String reason,
            OffsetDateTime createdAt
    ) {
        static PaymentResponse from(Payment payment) {
            return new PaymentResponse(
                    payment.getOrderId(),
                    payment.getStatus(),
                    payment.getAmount(),
                    payment.getMethod(),
                    payment.getReason(),
                    payment.getCreatedAt());
        }
    }
}
