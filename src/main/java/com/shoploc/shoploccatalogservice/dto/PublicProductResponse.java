package com.shoploc.shoploccatalogservice.dto;

import com.shoploc.shoploccatalogservice.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Vue publique d'un produit : pas de stock exact ni d'identifiant commerçant (voir InternalProductResponse).")
public record PublicProductResponse(
    @Schema(example = "1") Long id,
    @Schema(example = "Pain au levain") String name,
    @Schema(example = "Pain traditionnel cuit au feu de bois") String description,
    @Schema(example = "4.50") BigDecimal price,
    @Schema(nullable = true) String imageUrl,
    @Schema(description = "true si le stock est supérieur à 0") boolean available,
    @Schema(example = "1") Long commerceId,
    @Schema(example = "3") Long categoryId
) {
    public static PublicProductResponse fromEntity(Product p) {
        return new PublicProductResponse(
            p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getImageUrl(),
            p.getStock() != null && p.getStock() > 0,
            p.getCommerce() != null ? p.getCommerce().getId() : null,
            p.getCategory() != null ? p.getCategory().getId() : null);
    }
}
