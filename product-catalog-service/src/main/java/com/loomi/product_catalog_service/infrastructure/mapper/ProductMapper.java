package com.loomi.product_catalog_service.infrastructure.mapper;

import com.loomi.product_catalog_service.api.dto.ProductDto;
import com.loomi.product_catalog_service.domain.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductDto toDto(Product product);
}
