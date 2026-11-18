package com.sahidanwar.inventory;

import com.sahidanwar.inventory.domain.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @Test
    void reserveDecreasesStockAndRejectsOversell() {
        Product product = Product.create("WIDGET", "Widget", new BigDecimal("25.00"), 2);
        product.reserve(2);
        assertThat(product.getAvailable()).isZero();
        assertThatThrownBy(() -> product.reserve(1)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void releaseRestoresStock() {
        Product product = Product.create("WIDGET", "Widget", new BigDecimal("25.00"), 1);
        product.reserve(1);
        product.release(1);
        assertThat(product.getAvailable()).isEqualTo(1);
    }
}
