package com.ecommerce.order_processing_system.config;

import com.ecommerce.order_processing_system.domain.ProductType;
import com.ecommerce.order_processing_system.domain.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.Map;

@Configuration
public class ValidatorConfig {

    @Bean
    public ProductValidatorFactory productValidatorFactory(
            PhysicalProductValidator physicalValidator,
            SubscriptionProductValidator subscriptionValidator,
            DigitalProductValidator digitalValidator,
            PreOrderProductValidator preOrderValidator,
            CorporateProductValidator corporateValidator
    ) {
        Map<ProductType, ProductValidator> map = new EnumMap<>(ProductType.class);
        map.put(ProductType.PHYSICAL, physicalValidator);
        map.put(ProductType.SUBSCRIPTION, subscriptionValidator);
        map.put(ProductType.DIGITAL, digitalValidator);
        map.put(ProductType.PRE_ORDER, preOrderValidator);
        map.put(ProductType.CORPORATE, corporateValidator);

        return new ProductValidatorFactory(map);
    }
}
