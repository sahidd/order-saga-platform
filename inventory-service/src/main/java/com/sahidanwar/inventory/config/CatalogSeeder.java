package com.sahidanwar.inventory.config;

import com.sahidanwar.inventory.domain.Product;
import com.sahidanwar.inventory.infrastructure.ProductRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CatalogSeeder implements ApplicationRunner {

    private final ProductRepository products;

    public CatalogSeeder(ProductRepository products) {
        this.products = products;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (products.count() > 0) {
            return;
        }
        products.save(Product.create("WIDGET", "Widget", new BigDecimal("25.00"), 100));
        products.save(Product.create("GADGET", "Gadget", new BigDecimal("10.00"), 1));
    }
}
