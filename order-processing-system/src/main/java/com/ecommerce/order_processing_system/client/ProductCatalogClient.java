package com.ecommerce.order_processing_system.client;

import com.ecommerce.order_processing_system.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-catalog-service", url = "${app.product-catalog.url}")
public interface ProductCatalogClient {

    @GetMapping("/api/products/{productId}")
    ProductDTO getProduct(@PathVariable("productId") String productId);
}