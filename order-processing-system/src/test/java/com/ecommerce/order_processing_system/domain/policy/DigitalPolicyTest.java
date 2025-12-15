package com.ecommerce.order_processing_system.domain.policy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DigitalPolicyTest {

    private DigitalPolicy digitalPolicy;

    @BeforeEach
    void setUp() {
        digitalPolicy = new DigitalPolicy();
    }

    @Test
    void shouldSendEmailForDigitalProduct() {
        assertDoesNotThrow(() ->
                digitalPolicy.sendEmail("ORDER-123", "LICENSE-XYZ")
        );
    }
}
