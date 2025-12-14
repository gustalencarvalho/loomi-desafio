package com.loomi.product_catalog_service.infrastructure;

import com.loomi.product_catalog_service.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findByProductId(String productId);
}
