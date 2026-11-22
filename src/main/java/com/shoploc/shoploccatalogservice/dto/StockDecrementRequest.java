package com.shoploc.shoploccatalogservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Appelé par order-service à la validation d'un panier.")
public record StockDecrementRequest(
        @NotEmpty(message = "La liste d'articles est obligatoire")
        @Valid
        List<Item> items
) {
    public record Item(
            @Schema(example = "1")
            @NotNull(message = "L'identifiant de l'article est obligatoire")
            Long productId,

            @Schema(example = "2")
            @NotNull(message = "La quantité est obligatoire")
            @Min(value = 1, message = "La quantité doit être au moins 1")
            Integer quantity
    ) {}
}
