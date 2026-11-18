package com.shoploc.shoploccatalogservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record StockDecrementRequest(
        @NotEmpty(message = "La liste d'articles est obligatoire")
        @Valid
        List<Item> items
) {
    public record Item(
            @NotNull(message = "L'identifiant de l'article est obligatoire")
            Long productId,

            @NotNull(message = "La quantité est obligatoire")
            @Min(value = 1, message = "La quantité doit être au moins 1")
            Integer quantity
    ) {}
}