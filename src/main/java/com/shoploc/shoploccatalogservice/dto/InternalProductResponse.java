package com.shoploc.shoploccatalogservice.dto;

import com.shoploc.shoploccatalogservice.entity.Product;

import java.math.BigDecimal;

public record InternalProductResponse(
        Long id,
        String name,
        BigDecimal price,
        Boolean active,
        Integer stock,
        String merchantId
) {
    public static InternalProductResponse fromEntity(Product p) {
        return new InternalProductResponse(
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getActive(),
                p.getStock(),
                p.getCommerce() != null ? p.getCommerce().getIdCommercant() : null);
    }
}