package com.sahidanwar.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @Column(length = 40)
    private String sku;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int available;

    @Version
    @Column(nullable = false)
    private long version;

    protected Product() {
    }

    public static Product create(String sku, String name, BigDecimal price, int available) {
        Product product = new Product();
        product.sku = sku;
        product.name = name;
        product.price = price.setScale(2);
        product.available = available;
        return product;
    }

    public void reserve(int quantity) {
        if (quantity > available) {
            throw new IllegalStateException("Not enough stock.");
        }
        available -= quantity;
    }

    public void release(int quantity) {
        available += quantity;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getAvailable() {
        return available;
    }
}
