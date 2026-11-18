package com.sahidanwar.orders.application;

import com.sahidanwar.orders.domain.OutboxEvent;
import com.sahidanwar.orders.infrastructure.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.TimeUnit;

@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxRepository outbox;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final TransactionTemplate transactionTemplate;

    public OutboxPublisher(OutboxRepository outbox, KafkaTemplate<String, String> kafkaTemplate,
                           TransactionTemplate transactionTemplate) {
        this.outbox = outbox;
        this.kafkaTemplate = kafkaTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-delay-ms:1000}")
    public void publish() {
        for (OutboxEvent event : outbox.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc()) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload()).get(5, TimeUnit.SECONDS);
                transactionTemplate.executeWithoutResult(status ->
                        outbox.findById(event.getId()).ifPresent(OutboxEvent::markPublished));
            } catch (Exception exception) {
                log.warn("Outbox event {} was not published: {}", event.getId(), exception.toString());
            }
        }
    }
}
