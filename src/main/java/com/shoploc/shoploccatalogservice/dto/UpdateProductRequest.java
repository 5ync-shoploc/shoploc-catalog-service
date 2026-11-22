package com.shoploc.shoploccatalogservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateProductRequest(
    @Schema(example = "Pain au levain")
    @NotBlank(message = "Le nom de l'article est obligatoire")
    String name,

    @Schema(example = "Pain traditionnel cuit au feu de bois")
    @NotBlank(message = "La description est obligatoire")
    String description,

    @Schema(example = "4.90")
    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    BigDecimal price,

    @Schema(example = "15")
    @NotNull(message = "Le stock est obligatoire")
    @Min(value = 0, message = "Le stock ne peut pas être négatif")
    Integer stock,

    @Schema(description = "Facultative", nullable = true) String imageUrl,
    @Schema(example = "5") @Min(0) Integer alertThreshold,
    @Schema(description = "Facultative : omise = catégorie inchangée", nullable = true) Long categoryId
) {}
