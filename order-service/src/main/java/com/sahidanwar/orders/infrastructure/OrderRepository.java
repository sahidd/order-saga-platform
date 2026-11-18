package com.sahidanwar.orders.infrastructure;

import com.sahidanwar.orders.domain.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<CustomerOrder, UUID> {
}
