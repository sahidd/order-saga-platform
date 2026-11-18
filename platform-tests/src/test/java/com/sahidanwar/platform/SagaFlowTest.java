package com.sahidanwar.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.sahidanwar.gateway.GatewayApplication;
import com.sahidanwar.inventory.InventoryApplication;
import com.sahidanwar.orders.OrderApplication;
import com.sahidanwar.payments.PaymentApplication;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class SagaFlowTest {

    private static final String JWT_SECRET = "test-only-jwt-secret-change-me-please-32b";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("orderdb")
            .withUsername("saga")
            .withPassword("saga");

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.1"));

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    private static ConfigurableApplicationContext inventory;
    private static ConfigurableApplicationContext payment;
    private static ConfigurableApplicationContext orders;
    private static ConfigurableApplicationContext gateway;
    private static RestClient client;

    @BeforeAll
    static void startPlatform() throws Exception {
        createDatabases();
        inventory = start(InventoryApplication.class, "inventory-service", "inventorydb");
        payment = start(PaymentApplication.class, "payment-service", "paymentdb");
        orders = start(OrderApplication.class, "order-service", "orderdb");
        gateway = startGateway(port(orders), port(inventory), port(payment));
        client = RestClient.builder()
                .baseUrl("http://127.0.0.1:" + port(gateway))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token())
                .build();
    }

    @AfterAll
    static void stopPlatform() {
        close(gateway);
        close(orders);
        close(payment);
        close(inventory);
    }

    @Test
    void confirmsAnOrderAndReservesStock() {
        int before = available("WIDGET");
        UUID id = place("WIDGET", 1, "card");
        awaitStatus(id, "CONFIRMED");

        JsonNode order = get("/api/orders/" + id);
        assertThat(order.get("customer").asText()).isEqualTo("demo");
        assertThat(order.get("amount").decimalValue()).isEqualByComparingTo("25.00");
        assertThat(eventTypes(order)).contains("OrderCreated", "InventoryReserved", "PaymentProcessed");
        assertThat(available("WIDGET")).isEqualTo(before - 1);
        assertThat(get("/api/payments/" + id).get("status").asText()).isEqualTo("PROCESSED");

        JsonNode answer = post("/api/ops/ask", Map.of(
                "orderId", id.toString(),
                "question", "What happened to this order?"));
        assertThat(answer.get("source").asText()).isEqualTo("fallback");
        assertThat(answer.get("answer").asText()).contains("completed");
    }

    @Test
    void releasesStockWhenPaymentIsDeclined() {
        int before = available("WIDGET");
        UUID id = place("WIDGET", 1, "declined");
        awaitStatus(id, "CANCELLED");

        JsonNode order = get("/api/orders/" + id);
        assertThat(eventTypes(order)).contains("PaymentFailed", "OrderCancelled", "InventoryReleased");
        assertThat(available("WIDGET")).isEqualTo(before);
        assertThat(get("/api/payments/" + id).get("status").asText()).isEqualTo("FAILED");

        JsonNode answer = post("/api/ops/ask", Map.of(
                "orderId", id.toString(),
                "question", "Why did this order fail?"));
        assertThat(answer.get("source").asText()).isEqualTo("fallback");
        assertThat(answer.get("answer").asText()).contains("payment step failed");
    }

    @Test
    void cancelsWhenStockIsShort() {
        int before = available("GADGET");
        UUID id = place("GADGET", 2, "card");
        awaitStatus(id, "CANCELLED");

        JsonNode order = get("/api/orders/" + id);
        assertThat(eventTypes(order)).contains("InventoryRejected");
        assertThat(eventTypes(order)).doesNotContain("OrderCancelled");
        assertThat(available("GADGET")).isEqualTo(before);
    }

    private static void createDatabases() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             Statement statement = connection.createStatement()) {
            connection.setAutoCommit(true);
            statement.execute("CREATE DATABASE inventorydb");
            statement.execute("CREATE DATABASE paymentdb");
        }
    }

    private static ConfigurableApplicationContext start(Class<?> type, String configName, String database) {
        return new SpringApplicationBuilder(type).run(serviceArguments(configName, database));
    }

    private static ConfigurableApplicationContext startGateway(int orderPort, int inventoryPort, int paymentPort) {
        List<String> args = new ArrayList<>(List.of(baseArguments("gateway")));
        args.add("--spring.main.web-application-type=reactive");
        args.add("--spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration");
        args.add("--app.routes.order=http://127.0.0.1:" + orderPort);
        args.add("--app.routes.inventory=http://127.0.0.1:" + inventoryPort);
        args.add("--app.routes.payment=http://127.0.0.1:" + paymentPort);
        return new SpringApplicationBuilder(GatewayApplication.class).run(args.toArray(String[]::new));
    }

    private static String[] serviceArguments(String configName, String database) {
        List<String> args = new ArrayList<>(List.of(baseArguments(configName)));
        args.add("--spring.datasource.url=" + databaseUrl(database));
        args.add("--spring.datasource.username=" + POSTGRES.getUsername());
        args.add("--spring.datasource.password=" + POSTGRES.getPassword());
        args.add("--spring.kafka.bootstrap-servers=" + KAFKA.getBootstrapServers());
        args.add("--spring.data.redis.host=" + REDIS.getHost());
        args.add("--spring.data.redis.port=" + REDIS.getMappedPort(6379));
        return args.toArray(String[]::new);
    }

    private static String[] baseArguments(String configName) {
        return new String[]{
                "--spring.config.name=" + configName,
                "--spring.profiles.active=test",
                "--server.port=0",
                "--spring.main.banner-mode=off",
                "--spring.ai.model.chat=none",
                "--spring.ai.model.embedding=none",
                "--spring.ai.model.image=none",
                "--spring.ai.model.moderation=none",
                "--spring.ai.model.audio.speech=none",
                "--spring.ai.model.audio.transcription=none"
        };
    }

    private static String databaseUrl(String name) {
        String url = POSTGRES.getJdbcUrl();
        String current = "/" + POSTGRES.getDatabaseName();
        int index = url.indexOf(current);
        if (index < 0) {
            throw new IllegalStateException("Database name missing from " + url);
        }
        return url.substring(0, index) + "/" + name + url.substring(index + current.length());
    }

    private static String token() {
        SecretKey key = new SecretKeySpec(JWT_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("demo")
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    private static int port(ConfigurableApplicationContext context) {
        return Integer.parseInt(context.getEnvironment().getProperty("local.server.port"));
    }

    private static UUID place(String sku, int quantity, String paymentMethod) {
        JsonNode created = post("/api/orders", Map.of(
                "sku", sku,
                "quantity", quantity,
                "paymentMethod", paymentMethod));
        return UUID.fromString(created.get("id").asText());
    }

    private static void awaitStatus(UUID id, String status) {
        Awaitility.await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(200)).untilAsserted(() ->
                assertThat(get("/api/orders/" + id).get("status").asText()).isEqualTo(status));
    }

    private static int available(String sku) {
        return get("/api/products/" + sku).get("available").asInt();
    }

    private static List<String> eventTypes(JsonNode order) {
        List<String> types = new ArrayList<>();
        order.get("events").forEach(event -> types.add(event.get("type").asText()));
        return types;
    }

    private static JsonNode get(String path) {
        return client.get().uri(path).retrieve().body(JsonNode.class);
    }

    private static JsonNode post(String path, Object body) {
        return client.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
    }

    private static void close(ConfigurableApplicationContext context) {
        if (context != null) {
            context.close();
        }
    }
}
