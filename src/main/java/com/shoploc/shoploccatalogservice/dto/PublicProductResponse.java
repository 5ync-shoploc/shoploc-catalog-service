package com.shoploc.shoploccatalogservice.dto;

import com.shoploc.shoploccatalogservice.entity.Product;
import java.math.BigDecimal;

public record PublicProductResponse(
    Long id, String name, String description, BigDecimal price, String imageUrl,
    boolean available, Long commerceId, Long categoryId
) {
    public static PublicProductResponse fromEntity(Product p) {
        return new PublicProductResponse(
            p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getImageUrl(),
            p.getStock() != null && p.getStock() > 0,
            p.getCommerce() != null ? p.getCommerce().getId() : null,
            p.getCategory() != null ? p.getCategory().getId() : null);
    }
}