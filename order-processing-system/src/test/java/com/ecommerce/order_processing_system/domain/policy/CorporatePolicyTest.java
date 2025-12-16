package com.ecommerce.order_processing_system.domain.policy;

import com.ecommerce.order_processing_system.exception.InvalidPaymentTermsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CorporatePolicyTest {

    private CorporatePolicy corporatePolicy;

    @BeforeEach
    void setUp() {
        corporatePolicy = new CorporatePolicy();
    }

    @Test
    void shouldAcceptNet30PaymentTerms() {
        assertDoesNotThrow(() ->
                corporatePolicy.calculateDeliveryDate("NET_30")
        );
    }

    @Test
    void shouldAcceptNet60PaymentTerms() {
        assertDoesNotThrow(() ->
                corporatePolicy.calculateDeliveryDate("NET_60")
        );
    }

    @Test
    void shouldAcceptNet90PaymentTerms() {
        assertDoesNotThrow(() ->
                corporatePolicy.calculateDeliveryDate("NET_90")
        );
    }

    @Test
    void shouldThrowExceptionForInvalidPaymentTerms() {
        assertThrows(
                InvalidPaymentTermsException.class,
                () -> corporatePolicy.calculateDeliveryDate("INVALID")
        );
    }
}
