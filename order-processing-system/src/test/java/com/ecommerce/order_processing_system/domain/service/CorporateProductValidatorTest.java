package com.ecommerce.order_processing_system.domain.service;

import com.ecommerce.order_processing_system.domain.Order;
import com.ecommerce.order_processing_system.domain.OrderItem;
import com.ecommerce.order_processing_system.domain.policy.CorporatePolicy;
import com.ecommerce.order_processing_system.exception.CreditLimitExceededException;
import com.ecommerce.order_processing_system.exception.InvalidCorporateDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CorporateProductValidatorTest {

    @Mock
    private CorporatePolicy corporatePolicy;

    private CorporateProductValidator validator;

    private Order order;
    private OrderItem item;

    @BeforeEach
    void setUp() {
        validator = new CorporateProductValidator(corporatePolicy);

        // simula @Value
        ReflectionTestUtils.setField(validator, "corporateVolumeThreshold", 100);
        ReflectionTestUtils.setField(validator, "highValueThreshold", "100000");

        item = OrderItem.builder()
                .productId("CORP-1")
                .quantity(6)
                .metadata(new HashMap<>(Map.of(
                        "cnpj", "11222333000181", // CNPJ válido
                        "paymentTerms", "NET_30"
                )))
                .build();

        order = Order.builder()
                .orderId("ORDER-1")
                .items(List.of(item))
                .totalAmount(new BigDecimal("1000.00"))
                .build();
    }

    @Test
    void shouldThrowExceptionWhenCnpjIsInvalid() {
        item.getMetadata().put("cnpj", "123");

        assertThrows(
                InvalidCorporateDataException.class,
                () -> validator.validate(order, item)
        );
    }

    @Test
    void shouldThrowExceptionWhenCreditLimitExceeded() {
        order.setTotalAmount(new BigDecimal("100001"));

        assertThrows(
                CreditLimitExceededException.class,
                () -> validator.validate(order, item)
        );
    }

    @Test
    void shouldCallCorporatePolicyWhenPaymentTermsPresent() {
        validator.validate(order, item);
        verify(corporatePolicy).calculateDeliveryDate("NET_30");
    }

    @Test
    void shouldThrowExceptionWhenMetadataIsNull() {
        item.setMetadata(null);

        assertThrows(
                InvalidCorporateDataException.class,
                () -> validator.validate(order, item)
        );
    }

    @Test
    void shouldThrowExceptionWhenCnpjIsMissing() {
        item.getMetadata().remove("cnpj");

        assertThrows(
                InvalidCorporateDataException.class,
                () -> validator.validate(order, item)
        );
    }

    @Test
    void shouldNotApplyDiscountWhenVolumeBelowThreshold() {
        // quantidade total = 6 < 10
        validator.validate(order, item);

        assertEquals(new BigDecimal("1000.00"), order.getTotalAmount());
    }

    @Test
    void shouldNotCallCorporatePolicyWhenPaymentTermsNotPresent() {
        item.getMetadata().remove("paymentTerms");

        validator.validate(order, item);

        verify(corporatePolicy, never())
                .calculateDeliveryDate(any());
    }

    @Test
    void shouldValidateCorporateOrderSuccessfully() {
        validator.validate(order, item);

        assertEquals("ORDER-1", order.getOrderId());
    }

    @Test
    void shouldNotApplyDiscountWhenVolumeEqualsThreshold() {
        OrderItem item2 = OrderItem.builder()
                .quantity(4) // 6 + 4 = 10
                .metadata(Map.of("cnpj", "11222333000181"))
                .build();

        order.setItems(List.of(item, item2));

        validator.validate(order, item);

        assertEquals(new BigDecimal("1000.00"), order.getTotalAmount());
    }

    @Test
    void shouldThrowExceptionWhenAnyItemHasNullMetadata() {
        item.setMetadata(null);

        assertThrows(
                InvalidCorporateDataException.class,
                () -> validator.validate(order, item)
        );
    }

    @Test
    void shouldNotApplyDiscountWhenQuantityBelowThreshold() {
        validator.validate(order, item);

        assertEquals(
                0,
                order.getTotalAmount().compareTo(new BigDecimal("1000.00"))
        );
    }

    @Test
    void shouldNotCallCorporatePolicyWhenPaymentTermsIsMissing() {
        item.getMetadata().remove("paymentTerms");

        validator.validate(order, item);

        verify(corporatePolicy, never()).calculateDeliveryDate(any());
    }

    @Test
    void shouldValidateCorporateOrderWithoutDiscount() {
        validator.validate(order, item);
    }

}
