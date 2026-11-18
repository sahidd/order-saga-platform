package com.sahidanwar.payments.infrastructure;

import com.sahidanwar.payments.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
