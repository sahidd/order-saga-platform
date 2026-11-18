package com.sahidanwar.inventory;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InventoryApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(InventoryApplication.class)
                .properties("spring.config.name=inventory-service")
                .run(args);
    }
}
