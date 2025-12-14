package com.loomi.product_catalog_service.application;

import com.loomi.product_catalog_service.api.dto.ProductDto;
import com.loomi.product_catalog_service.domain.Product;
import com.loomi.product_catalog_service.domain.exeception.ProductNotFoundException;
import com.loomi.product_catalog_service.infrastructure.ProductRepository;
import com.loomi.product_catalog_service.infrastructure.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper mapper;

    @Transactional(readOnly = true)
    public ProductDto getProduct(String productId) {
        log.info("GET product for ID: {} ", productId);
        return productRepository.findByProductId(productId)
                .map(mapper::toDto)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product " + productId + " not found or inactive"
                ));
    }

    @Transactional
    public boolean reserveStock(String productId, int quantity) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product " + productId + " not found"
                ));

        if (product.getStockQuantity() < quantity) {
            return false;
        }

        product.setStockQuantity(quantity);
        productRepository.save(product);
        return true;
    }

    @Transactional
    public boolean reservePreOrder(String productId, int quantity) {
        Product product = productRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product " + productId + " not found"
                ));

        int availableSlots = Integer.parseInt(product.getMetadata().get("preOrderSlots").toString());

        if (availableSlots < quantity) {
            return false;
        }

        product.getMetadata().put("preOrderSlots", availableSlots - quantity);
        productRepository.save(product);
        return true;
    }

}
