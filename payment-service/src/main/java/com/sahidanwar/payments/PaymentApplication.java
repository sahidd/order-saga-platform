package com.sahidanwar.payments;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PaymentApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(PaymentApplication.class)
                .properties("spring.config.name=payment-service")
                .run(args);
    }
}
