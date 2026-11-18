package com.sahidanwar.orders.infrastructure;

import com.sahidanwar.orders.domain.SagaRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SagaRecordRepository extends JpaRepository<SagaRecord, UUID> {

    List<SagaRecord> findByOrderIdOrderByCreatedAtAsc(UUID orderId);
}
