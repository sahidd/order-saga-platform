package com.sahidanwar.inventory.infrastructure;

import com.sahidanwar.inventory.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
}
