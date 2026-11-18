package com.sahidanwar.orders.application;

import com.sahidanwar.platform.SagaNarrator;
import com.sahidanwar.orders.domain.CustomerOrder;
import com.sahidanwar.orders.domain.DomainException;
import com.sahidanwar.orders.infrastructure.OrderRepository;
import com.sahidanwar.orders.infrastructure.SagaRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AskService {

    private static final Logger log = LoggerFactory.getLogger(AskService.class);
    private static final String SYSTEM = """
            Explain this order saga in plain English.
            Use only the status and event names provided. Name the step that failed when one did.
            Do not invent steps, refunds, or inventory changes that are not in the event list.
            """;

    private final OrderRepository orders;
    private final SagaRecordRepository history;
    private final ObjectProvider<TextGenerator> textGenerator;

    public AskService(OrderRepository orders, SagaRecordRepository history, ObjectProvider<TextGenerator> textGenerator) {
        this.orders = orders;
        this.history = history;
        this.textGenerator = textGenerator;
    }

    @Transactional(readOnly = true)
    public Explanation explain(UUID orderId, String question) {
        CustomerOrder order = orders.findById(orderId)
                .orElseThrow(() -> new DomainException("not-found", "Order was not found.", 404));
        List<String> types = history.findByOrderIdOrderByCreatedAtAsc(orderId).stream()
                .map(record -> record.getEventType())
                .toList();
        String fallback = SagaNarrator.explain(order.getStatus().name(), types);
        TextGenerator generator = textGenerator.getIfAvailable();
        if (generator != null) {
            try {
                String generated = generator.generate(SYSTEM, """
                        Question: %s
                        Status: %s
                        Events: %s
                        """.formatted(question, order.getStatus(), types));
                if (generated != null && !generated.isBlank()) {
                    return new Explanation(generated.trim(), "openai");
                }
            } catch (RuntimeException exception) {
                log.warn("Saga explanation model failed: {}", exception.toString());
            }
        }
        return new Explanation(fallback, "fallback");
    }

    public record Explanation(String answer, String source) {
    }
}
