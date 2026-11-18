package com.sahidanwar.payments.config;

import com.sahidanwar.platform.SagaTypes;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    NewTopic paymentTopic() {
        return new NewTopic(SagaTypes.PAYMENT_TOPIC, 1, (short) 1);
    }
}
