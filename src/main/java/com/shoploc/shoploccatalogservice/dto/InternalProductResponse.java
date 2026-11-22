package com.shoploc.shoploccatalogservice.dto;

import com.shoploc.shoploccatalogservice.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Réservée à order-service : contient le stock exact et l'identifiant du commerçant.")
public record InternalProductResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Pain au levain") String name,
        @Schema(example = "4.50") BigDecimal price,
        @Schema(description = "false = produit désactivé (suppression logique)") Boolean active,
        @Schema(example = "20") Integer stock,
        @Schema(description = "Identifiant Keycloak du commerçant propriétaire") String merchantId
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
