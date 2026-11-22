package com.shoploc.shoploccatalogservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Déclare le contrat d'API (titre affiché dans Swagger UI).
 * Swagger UI : /swagger-ui.html — JSON du contrat : /v3/api-docs.
 *
 * Pas de schéma d'authentification ici : ce service ne vérifie pas de JWT.
 * Les endpoints commerçant lisent X-User-Id / X-User-Role (en-têtes de
 * confiance, non vérifiés par ce service — voir CLAUDE.md local).
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "ShopLoc – Catalog Service",
        version = "v1",
        description = "Produits, commerces, catégories. Deux endpoints sont réservés à order-service (section \"Interne\")."))
public class OpenApiConfig {
}
