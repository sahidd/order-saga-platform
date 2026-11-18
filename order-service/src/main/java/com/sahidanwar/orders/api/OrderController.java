package com.sahidanwar.orders.api;

import com.sahidanwar.orders.application.AskService;
import com.sahidanwar.orders.application.OrderCommandService;
import com.sahidanwar.orders.domain.CustomerOrder;
import com.sahidanwar.orders.domain.DomainException;
import com.sahidanwar.orders.domain.SagaRecord;
import com.sahidanwar.orders.infrastructure.OrderRepository;
import com.sahidanwar.orders.infrastructure.SagaRecordRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderCommandService commands;
    private final AskService askService;
    private final OrderRepository orders;
    private final SagaRecordRepository history;

    public OrderController(OrderCommandService commands, AskService askService, OrderRepository orders,
                           SagaRecordRepository history) {
        this.commands = commands;
        this.askService = askService;
        this.orders = orders;
        this.history = history;
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    OrderResponse create(@Valid @RequestBody CreateOrderRequest request, Authentication authentication) {
        CustomerOrder order = commands.place(
                authentication.getName(), request.sku(), request.quantity(), request.paymentMethod());
        return OrderResponse.from(order, List.of());
    }

    @GetMapping("/orders/{id}")
    OrderResponse get(@PathVariable UUID id) {
        CustomerOrder order = orders.findById(id)
                .orElseThrow(() -> new DomainException("not-found", "Order was not found.", 404));
        List<EventResponse> events = history.findByOrderIdOrderByCreatedAtAsc(id).stream()
                .map(EventResponse::from)
                .toList();
        return OrderResponse.from(order, events);
    }

    @PostMapping("/ops/ask")
    AskResponse ask(@Valid @RequestBody AskRequest request) {
        AskService.Explanation explanation = askService.explain(request.orderId(), request.question());
        return new AskResponse(explanation.answer(), explanation.source());
    }

    public record CreateOrderRequest(@NotBlank String sku, @Min(1) int quantity, @NotBlank String paymentMethod) {
    }

    public record AskRequest(@NotNull UUID orderId, @NotBlank String question) {
    }

    public record AskResponse(String answer, String source) {
    }

    public record EventResponse(UUID eventId, String type, OffsetDateTime createdAt) {
        static EventResponse from(SagaRecord record) {
            return new EventResponse(record.getEventId(), record.getEventType(), record.getCreatedAt());
        }
    }

    public record OrderResponse(
            UUID id,
            String customer,
            String sku,
            int quantity,
            BigDecimal amount,
            String paymentMethod,
            String status,
            List<EventResponse> events
    ) {
        static OrderResponse from(CustomerOrder order, List<EventResponse> events) {
            return new OrderResponse(
                    order.getId(),
                    order.getCustomerName(),
                    order.getSku(),
                    order.getQuantity(),
                    order.getAmount(),
                    order.getPaymentMethod(),
                    order.getStatus().name(),
                    events);
        }
    }
}
