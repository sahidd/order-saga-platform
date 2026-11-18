package com.sahidanwar.inventory.application;

import com.sahidanwar.inventory.domain.OutboxEvent;
import com.sahidanwar.inventory.domain.ProcessedEvent;
import com.sahidanwar.inventory.domain.Product;
import com.sahidanwar.inventory.domain.Reservation;
import com.sahidanwar.inventory.infrastructure.OutboxRepository;
import com.sahidanwar.inventory.infrastructure.ProcessedEventRepository;
import com.sahidanwar.inventory.infrastructure.ProductRepository;
import com.sahidanwar.inventory.infrastructure.ReservationRepository;
import com.sahidanwar.platform.SagaEvent;
import com.sahidanwar.platform.SagaJson;
import com.sahidanwar.platform.SagaTypes;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class InventoryService {

    private final ProductRepository products;
    private final ReservationRepository reservations;
    private final OutboxRepository outbox;
    private final ProcessedEventRepository processed;

    public InventoryService(ProductRepository products, ReservationRepository reservations, OutboxRepository outbox,
                            ProcessedEventRepository processed) {
        this.products = products;
        this.reservations = reservations;
        this.outbox = outbox;
        this.processed = processed;
    }

    @Cacheable(cacheNames = "products", key = "#sku")
    @Transactional(readOnly = true)
    public ProductView get(String sku) {
        return products.findById(sku)
                .map(ProductView::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product was not found."));
    }

    @CacheEvict(cacheNames = "products", key = "#event.sku()")
    @Transactional
    public void onOrderCreated(SagaEvent event) {
        if (!accept(event.eventId())) {
            return;
        }
        Product product = products.findById(event.sku()).orElse(null);
        if (product == null || product.getAvailable() < event.quantity()) {
            publish(event, SagaTypes.INVENTORY_REJECTED, BigDecimal.ZERO.setScale(2), "Not enough stock.");
            return;
        }
        product.reserve(event.quantity());
        reservations.save(Reservation.hold(event.orderId(), event.sku(), event.quantity()));
        BigDecimal amount = product.getPrice()
                .multiply(BigDecimal.valueOf(event.quantity()))
                .setScale(2, RoundingMode.UNNECESSARY);
        publish(event, SagaTypes.INVENTORY_RESERVED, amount, null);
    }

    @CacheEvict(cacheNames = "products", key = "#event.sku()")
    @Transactional
    public void onOrderCancelled(SagaEvent event) {
        if (!accept(event.eventId())) {
            return;
        }
        Reservation reservation = reservations.findById(event.orderId()).orElse(null);
        if (reservation == null || reservation.isReleased()) {
            return;
        }
        products.findById(reservation.getSku()).ifPresent(product -> product.release(reservation.getQuantity()));
        reservation.release();
        publish(event, SagaTypes.INVENTORY_RELEASED, event.amount(), "Reservation released.");
    }

    private boolean accept(UUID eventId) {
        if (processed.existsById(eventId)) {
            return false;
        }
        processed.save(ProcessedEvent.of(eventId));
        return true;
    }

    private void publish(SagaEvent source, String type, BigDecimal amount, String reason) {
        SagaEvent outgoing = new SagaEvent(
                UUID.randomUUID(),
                type,
                source.orderId(),
                source.sku(),
                source.quantity(),
                amount,
                source.paymentMethod(),
                reason);
        outbox.save(OutboxEvent.unpublished(
                SagaTypes.INVENTORY_TOPIC, source.orderId().toString(), SagaJson.write(outgoing)));
    }

    public record ProductView(String sku, String name, BigDecimal price, int available) {
        static ProductView from(Product product) {
            return new ProductView(product.getSku(), product.getName(), product.getPrice(), product.getAvailable());
        }
    }
}
