package com.ecommerce.order_processing_system.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;

import java.time.Duration;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> orderPostgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("order_processing_db")
            .withUsername("postgres")
            .withPassword("15421542")
            .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*", 1))  // ✅
            .withStartupTimeout(Duration.ofSeconds(60));  // ✅

    @Container
    static RedpandaContainer redpanda = new RedpandaContainer("docker.redpanda.com/redpandadata/redpanda:v23.3.10")
            .waitingFor(Wait.forLogMessage(".*Kafka server.*started.*", 1))  // ✅
            .waitingFor(Wait.forLogMessage(".*redpanda.*started.*", 1))     // ✅
            .withStartupTimeout(Duration.ofSeconds(90));  // ✅

    @DynamicPropertySource
    static void configureProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", orderPostgres::getJdbcUrl);
        registry.add("spring.datasource.username", orderPostgres::getUsername);
        registry.add("spring.datasource.password", orderPostgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", redpanda::getBootstrapServers);

        // ✅ Configs seguras DEFAULT
        registry.add("app.order.topic.created", () -> "order-events-created");
        registry.add("app.order.topic.processed", () -> "order-events-processed");
        registry.add("app.order.topic.failed", () -> "order-events-failed");
        registry.add("app.order.fraud-check-threshold", () -> "1000.00");  // Desabilita fraud
        registry.add("app.order.low-stock-alert-threshold", () -> "1");    // Desabilita low stock
    }
}
