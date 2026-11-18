package com.sahidanwar.inventory.infrastructure;

import com.sahidanwar.inventory.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
}
