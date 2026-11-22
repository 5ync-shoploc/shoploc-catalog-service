package com.shoploc.shoploccatalogservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateProductRequest(
    @Schema(example = "Pain au levain") @NotBlank(message = "Le nom de l'article est obligatoire") String name,
    @Schema(example = "Pain traditionnel cuit au feu de bois") @NotBlank(message = "La description est obligatoire") String description,
    @Schema(example = "4.50") @NotNull @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0") BigDecimal price,
    @Schema(example = "20") @NotNull @Min(value = 0, message = "Le stock ne peut pas être négatif") Integer stock,
    @Schema(description = "Facultative", nullable = true) String imageUrl,
    @Schema(description = "Seuil en dessous duquel une alerte de rupture est déclenchée", example = "5")
    @Min(value = 0, message = "Le seuil d'alerte ne peut pas être négatif") Integer alertThreshold,
    @Schema(description = "Doit appartenir au commerçant connecté", example = "1")
    @NotNull(message = "Le commerce est obligatoire") Long commerceId,
    @Schema(example = "3") @NotNull(message = "La catégorie est obligatoire") Long categoryId
) {}
