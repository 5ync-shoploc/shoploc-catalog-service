package com.shoploc.shoploccatalogservice.controller;

import com.shoploc.shoploccatalogservice.dto.CreateProductRequest;
import com.shoploc.shoploccatalogservice.dto.InternalProductResponse;
import com.shoploc.shoploccatalogservice.dto.PublicProductResponse;
import com.shoploc.shoploccatalogservice.dto.StockDecrementRequest;
import com.shoploc.shoploccatalogservice.dto.UpdateProductRequest;
import com.shoploc.shoploccatalogservice.entity.Product;
import com.shoploc.shoploccatalogservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Produits", description = "Catalogue public, gestion par le commerçant, et endpoints internes à order-service.")
public class ProductController {

    private static final String ROLE_COMMERCANT = "COMMERCANT";

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @Operation(summary = "Consulter le catalogue", description = "Accès public. Seuls les produits actifs sont listés.")
    @GetMapping
    public Page<PublicProductResponse> getPublicCatalog(Pageable pageable) {
        return service.getPublicCatalog(pageable);
    }

    @Operation(summary = "Mes articles", description = "Réservé au commerçant propriétaire des articles.")
    @ApiResponse(responseCode = "200", description = "Mes articles")
    @ApiResponse(responseCode = "401", description = "En-têtes X-User-Id / X-User-Role manquants")
    @ApiResponse(responseCode = "403", description = "Rôle différent de COMMERCANT")
    @GetMapping("/my-products")
    public Page<Product> getMyProducts(
            @Parameter(description = "Identifiant de l'utilisateur connecté (en-tête de confiance posé par la Gateway)")
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @Parameter(description = "Rôle de l'utilisateur connecté", example = "COMMERCANT")
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            Pageable pageable
    ) {
        requireCommercant(userId, userRole, "Seul le commerçant peut consulter ses articles");
        return service.getMerchantProducts(userId, pageable);
    }

    @Operation(summary = "Créer un article", description = "Le commerce doit appartenir au commerçant connecté.")
    @ApiResponse(responseCode = "201", description = "Article créé")
    @ApiResponse(responseCode = "401", description = "En-têtes X-User-Id / X-User-Role manquants")
    @ApiResponse(responseCode = "403", description = "Rôle différent de COMMERCANT, ou commerce appartenant à un autre")
    @ApiResponse(responseCode = "404", description = "Commerce ou catégorie introuvable")
    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Valid @RequestBody CreateProductRequest request
    ) {
        requireCommercant(userId, userRole, "Seul le commerçant peut créer un article");
        Product created = service.createProduct(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Modifier un article", description = "L'article doit appartenir au commerçant connecté.")
    @ApiResponse(responseCode = "200", description = "Article modifié")
    @ApiResponse(responseCode = "403", description = "Rôle différent de COMMERCANT, ou article appartenant à un autre")
    @ApiResponse(responseCode = "404", description = "Article ou catégorie introuvable")
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        requireCommercant(userId, userRole, "Seul le commerçant peut modifier un article");
        Product updated = service.updateProduct(id, request, userId);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Supprimer un article", description = "Suppression logique (passe en inactif) : l'historique des commandes est conservé.")
    @ApiResponse(responseCode = "204", description = "Article désactivé")
    @ApiResponse(responseCode = "403", description = "Rôle différent de COMMERCANT, ou article appartenant à un autre")
    @ApiResponse(responseCode = "404", description = "Article introuvable")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        requireCommercant(userId, userRole, "Seul le commerçant peut supprimer un article");
        service.deleteProduct(id, userId);
        return ResponseEntity.noContent().build();
    }

    private void requireCommercant(String userId, String userRole, String forbiddenMessage) {
        if (userId == null || userId.isBlank() || userRole == null || userRole.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentification requise");
        }
        if (!ROLE_COMMERCANT.equalsIgnoreCase(userRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, forbiddenMessage);
        }
    }

    @Operation(summary = "Détail d'un article", description = "Accès public. 404 si inconnu ou inactif.")
    @GetMapping("/{id}")
    public PublicProductResponse getProductDetail(@PathVariable Long id) {
        return service.getProductDetail(id);
    }

    @Operation(summary = "[Interne] Détail d'un article pour order-service",
            description = "Appelé par order-service : contient le stock et l'identifiant du commerçant, absents de la réponse publique.")
    @GetMapping("/{id}/internal")
    public InternalProductResponse getProductInternal(@PathVariable Long id) {
        return service.getInternalProduct(id);
    }

    @Operation(summary = "[Interne] Décrémenter le stock pour order-service",
            description = "Appelé à la validation d'un panier. Décrément atomique (verrouillage en base) ; 409 si stock insuffisant.")
    @ApiResponse(responseCode = "204", description = "Stock décrémenté pour tous les articles demandés")
    @ApiResponse(responseCode = "404", description = "Un des articles est introuvable")
    @ApiResponse(responseCode = "409", description = "Stock insuffisant pour un des articles")
    @PostMapping("/stock/decrement")
    public ResponseEntity<Void> decrementStock(@Valid @RequestBody StockDecrementRequest request) {
        service.decrementStock(request);
        return ResponseEntity.noContent().build();
    }
}