package com.ecommerce.order_processing_system.domain.policy;

import com.ecommerce.order_processing_system.exception.IncompatibleSubscriptionsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SubscriptionPolicyTest {

    private SubscriptionPolicy subscriptionPolicy;

    @BeforeEach
    void setUp() {
        subscriptionPolicy = new SubscriptionPolicy();
    }

    // 🔹 BRANCH: não incompatível
    @Test
    void shouldNotThrowWhenSubscriptionsAreCompatible() {
        Set<String> previous = Set.of("SUB-BASIC-1");
        Set<String> current = Set.of("SUB-BASIC-2");

        assertDoesNotThrow(() ->
                subscriptionPolicy.validate(current, previous)
        );
    }

    // 🔹 BRANCH: ENTERPRISE → BASIC (incompatível)
    @Test
    void shouldThrowWhenEnterpriseToBasicIsIncompatible() {
        Set<String> previous = Set.of("SUB-ENTERPRISE-1");
        Set<String> current = Set.of("SUB-BASIC-1");

        assertThrows(
                IncompatibleSubscriptionsException.class,
                () -> subscriptionPolicy.validate(current, previous)
        );
    }

    // 🔹 BRANCH: BASIC → ENTERPRISE (incompatível)
    @Test
    void shouldThrowWhenBasicToEnterpriseIsIncompatible() {
        Set<String> previous = Set.of("SUB-BASIC-1");
        Set<String> current = Set.of("SUB-ENTERPRISE-1");

        assertThrows(
                IncompatibleSubscriptionsException.class,
                () -> subscriptionPolicy.validate(current, previous)
        );
    }

    // 🔹 BRANCH: múltiplos itens, um incompatível já deve falhar
    @Test
    void shouldThrowWhenAnySubscriptionIsIncompatible() {
        Set<String> previous = Set.of("SUB-BASIC-1", "SUB-BASIC-2");
        Set<String> current = Set.of("SUB-ENTERPRISE-1", "SUB-BASIC-3");

        assertThrows(
                IncompatibleSubscriptionsException.class,
                () -> subscriptionPolicy.validate(current, previous)
        );
    }
}
