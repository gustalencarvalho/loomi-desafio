package com.loomi.product_catalog_service.application;

import com.loomi.product_catalog_service.api.dto.ProductDto;
import com.loomi.product_catalog_service.domain.exeception.ProductNotFoundException;
import com.loomi.product_catalog_service.infrastructure.ProductRepository;
import com.loomi.product_catalog_service.infrastructure.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper mapper;

    @Transactional(readOnly = true)
    public ProductDto getProduct(String productId) {
        log.info("GET product for ID: {} ", productId);
        return productRepository.findByProductIdAndActiveTrue(productId)
                .map(mapper::toDto)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product " + productId + " not found"
                ));
    }

}
