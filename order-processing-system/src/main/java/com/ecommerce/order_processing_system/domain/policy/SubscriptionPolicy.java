package com.ecommerce.order_processing_system.domain.policy;

import com.ecommerce.order_processing_system.exception.IncompatibleSubscriptionsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.ecommerce.order_processing_system.domain.OrderStatus.INCOMPATIBLE_SUBSCRIPTIONS;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionPolicy {

    public void validate(Set<String> current, Set<String> previous) {
        boolean incompatible = current.stream().anyMatch(c ->  previous.stream().anyMatch(p -> isIncompatible(p, c)));

        if (incompatible) {
            log.info("Incompatible subscriptions");
            throw new IncompatibleSubscriptionsException(INCOMPATIBLE_SUBSCRIPTIONS);
        }
    }

    private boolean isIncompatible(String previousProductId, String newProductId) {
        if (previousProductId.startsWith("SUB-ENTERPRISE") && newProductId.startsWith("SUB-BASIC")) {
            return true;
        }
        if (previousProductId.startsWith("SUB-BASIC") && newProductId.startsWith("SUB-ENTERPRISE")) {
            return true;
        }
        return false;
    }
}
