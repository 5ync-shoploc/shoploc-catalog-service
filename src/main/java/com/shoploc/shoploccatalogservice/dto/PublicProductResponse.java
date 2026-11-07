package com.shoploc.shoploccatalogservice.dto;

import com.shoploc.shoploccatalogservice.entity.Product;
import java.math.BigDecimal;

public record PublicProductResponse(
    Long id,
    String name,
    String description,
    BigDecimal price,
    String imageUrl,
    boolean available
) {
    public static PublicProductResponse fromEntity(Product product) {
        return new PublicProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getImageUrl(),
            product.getStock() != null && product.getStock() > 0
        );
    }
}