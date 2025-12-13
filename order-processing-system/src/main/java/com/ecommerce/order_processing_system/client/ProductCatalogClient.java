package com.ecommerce.order_processing_system.client;

import com.ecommerce.order_processing_system.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-catalog-service", url = "${app.product-catalog.url}")
public interface ProductCatalogClient {

    @GetMapping("/api/products/{productId}")
    ProductDTO getProduct(@PathVariable String productId);

    @RequestMapping(method = RequestMethod.PATCH, value = "/api/products/{productId}/update/stock/{quantity}")
    boolean updateStock(@PathVariable("productId") String productId, @PathVariable("quantity") Integer quantity);

}