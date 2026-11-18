package com.sahidanwar.inventory.api;

import com.sahidanwar.inventory.application.InventoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final InventoryService inventory;

    public ProductController(InventoryService inventory) {
        this.inventory = inventory;
    }

    @GetMapping("/{sku}")
    InventoryService.ProductView get(@PathVariable String sku) {
        return inventory.get(sku);
    }
}
