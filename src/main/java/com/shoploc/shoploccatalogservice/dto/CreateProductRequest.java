package com.shoploc.shoploccatalogservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateProductRequest(
    @NotBlank(message = "Le nom de l'article est obligatoire")
    String name,

    @NotBlank(message = "La description est obligatoire")
    String description,

    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    BigDecimal price,

    @NotNull(message = "Le stock initial est obligatoire")
    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    Integer stock
) {}