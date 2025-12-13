package com.loomi.product_catalog_service.api;

import com.loomi.product_catalog_service.api.dto.ProductDto;
import com.loomi.product_catalog_service.application.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable String productId) {
        return ResponseEntity.ok(productService.getProduct(productId));
    }

    @PatchMapping("/{productId}/update/stock/{quantity}")
    public ResponseEntity<Boolean> updateStock(@PathVariable String productId, @PathVariable Integer quantity) {
        return ResponseEntity.ok().body(productService.reserveStock(productId, quantity));
    }

}
